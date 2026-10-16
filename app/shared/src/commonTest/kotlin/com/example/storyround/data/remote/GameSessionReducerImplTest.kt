package com.example.storyround.data.remote

import com.example.storyround.data.local.RemoteSession
import com.example.storyround.domain.model.GameError
import com.example.storyround.domain.model.GameState
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomSettings
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.ServerMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class GameSessionReducerImplTest {
    private val reducer = GameSessionReducerImpl()
    private val player = Player(id = "player-1", name = "Alex", colour = PlayerColour.Blue)
    private val room =
        RoomState(
            roomCode = "7F2K",
            hostId = player.id,
            players = listOf(player),
            status = GameStatus.Lobby,
            maxPlayers = 10,
            settings = RoomSettings(rounds = 3),
        )

    @Test
    fun `given an established session when room state arrives then state is updated and the session is persisted`() {
        // Given
        val context =
            GameSessionContext(
                state = GameState(playerId = player.id),
                sessionToken = "token-1",
                pendingPlayerName = null,
                isConnected = true,
            )

        // When
        val result = reducer.reduce(context, ServerMessage.RoomStateChanged(room))

        // Then
        assertEquals(room, result.context.state.room)
        assertNull(result.context.state.error)
        assertEquals(
            SessionPersistenceCommand.Save(RemoteSession(room.roomCode, player.id, "token-1")),
            result.persistenceCommand,
        )
    }

    @Test
    fun `given a pending player when joined event arrives then player identity is captured`() {
        // Given
        val context =
            GameSessionContext(
                state = GameState(),
                sessionToken = null,
                pendingPlayerName = player.name,
                isConnected = true,
            )

        // When
        val result = reducer.reduce(context, ServerMessage.PlayerJoined(player))

        // Then
        assertEquals(player.id, result.context.state.playerId)
        assertNull(result.context.pendingPlayerName)
        assertNull(result.persistenceCommand)
    }

    @Test
    fun `given an invalid session error when reduced then session data is cleared`() {
        // Given
        val context =
            GameSessionContext(
                state = GameState(room = room, playerId = player.id),
                sessionToken = "token-1",
                pendingPlayerName = player.name,
                isConnected = true,
            )

        // When
        val result = reducer.reduce(context, ServerMessage.Error("INVALID_SESSION", "Session expired."))

        // Then
        assertEquals(GameError.Server("Session expired."), result.context.state.error)
        assertEquals(true, result.context.state.isConnected)
        assertNull(result.context.state.room)
        assertNull(result.context.sessionToken)
        assertNull(result.context.pendingPlayerName)
        assertIs<SessionPersistenceCommand.Clear>(result.persistenceCommand)
    }

    @Test
    fun `given a saved session resumed on launch when the room no longer exists then no error is shown`() {
        // Given
        val context =
            GameSessionContext(
                state = GameState(playerId = player.id),
                sessionToken = "token-1",
                pendingPlayerName = null,
                isConnected = true,
            )

        // When
        val result = reducer.reduce(context, ServerMessage.Error("ROOM_NOT_FOUND", "That room does not exist."))

        // Then
        assertNull(result.context.state.error)
        assertNull(result.context.state.playerId)
        assertIs<SessionPersistenceCommand.Clear>(result.persistenceCommand)
    }

    @Test
    fun `should keep the server's message when a join is refused because the room does not exist`() {
        // Given
        val joining = GameSessionContext(state = GameState(), sessionToken = null, pendingPlayerName = "Sam", isConnected = true)

        // When
        val reduction = reducer.reduce(joining, ServerMessage.Error("ROOM_NOT_FOUND", ROOM_NOT_FOUND_MESSAGE))

        // Then
        assertEquals(GameError.Server(ROOM_NOT_FOUND_MESSAGE), reduction.context.state.error)
    }
}

private const val ROOM_NOT_FOUND_MESSAGE = "No game found with that code. Check it and try again."
