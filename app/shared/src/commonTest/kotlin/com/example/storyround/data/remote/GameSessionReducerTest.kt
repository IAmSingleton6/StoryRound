package com.example.storyround.data.remote

import com.example.storyround.data.local.RemoteSession
import com.example.storyround.domain.model.RemoteGameState
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.protocol.StorySummary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class GameSessionReducerTest {
    private val reducer = GameSessionReducer()
    private val player = Player(id = "player-1", name = "Alex", colour = PlayerColour.Blue)
    private val room =
        RoomState(
            roomCode = "7F2K",
            hostId = player.id,
            players = listOf(player),
            status = GameStatus.Lobby,
            maxPlayers = 10,
            maxRounds = 3,
        )

    @Test
    fun `given an established session when room state arrives then state is updated and the session is persisted`() {
        val context =
            GameSessionContext(
                state = RemoteGameState(playerId = player.id),
                sessionToken = "token-1",
                pendingPlayerName = null,
                isConnected = true,
            )

        val result = reducer.reduce(context, ServerMessage.RoomStateChanged(room))

        assertEquals(room, result.context.state.room)
        assertNull(result.context.state.error)
        assertEquals(
            SessionPersistenceCommand.Save(RemoteSession(room.roomCode, player.id, "token-1")),
            result.persistenceCommand,
        )
    }

    @Test
    fun `given a pending player when joined event arrives then player identity is captured`() {
        val context =
            GameSessionContext(
                state = RemoteGameState(),
                sessionToken = null,
                pendingPlayerName = player.name,
                isConnected = true,
            )

        val result = reducer.reduce(context, ServerMessage.PlayerJoined(player))

        assertEquals(player.id, result.context.state.playerId)
        assertNull(result.context.pendingPlayerName)
        assertNull(result.persistenceCommand)
    }

    @Test
    fun `given an invalid session error when reduced then session data is cleared`() {
        val context =
            GameSessionContext(
                state = RemoteGameState(room = room, playerId = player.id, history = emptyList()),
                sessionToken = "token-1",
                pendingPlayerName = player.name,
                isConnected = true,
            )

        val result = reducer.reduce(context, ServerMessage.Error("INVALID_SESSION", "Session expired."))

        assertEquals("Session expired.", result.context.state.error)
        assertEquals(true, result.context.state.isConnected)
        assertNull(result.context.state.room)
        assertNull(result.context.sessionToken)
        assertNull(result.context.pendingPlayerName)
        assertIs<SessionPersistenceCommand.Clear>(result.persistenceCommand)
    }

    @Test
    fun `given saved stories when an invalid session error is reduced then local history is kept`() {
        val history = listOf(StorySummary("story-1", "A door opened.", 1, 1, 1))
        val context =
            GameSessionContext(
                state = RemoteGameState(room = room, playerId = player.id, history = history),
                sessionToken = "token-1",
                pendingPlayerName = null,
                isConnected = true,
            )

        val result = reducer.reduce(context, ServerMessage.Error("INVALID_SESSION", "Session expired."))

        assertNull(result.context.state.room)
        assertEquals(history, result.context.state.history)
    }
}
