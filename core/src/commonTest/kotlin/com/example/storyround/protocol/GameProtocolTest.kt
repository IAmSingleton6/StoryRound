package com.example.storyround.protocol

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GameProtocolTest {
    private val json = protocolJson()

    @Test
    fun `given create room message when encoded then it round trips`() {
        // Given
        val message: ClientMessage = ClientMessage.CreateRoom("Alex")

        // When
        val decoded = json.decodeFromString<ClientMessage>(json.encodeToString(ClientMessage.serializer(), message))

        // Then
        assertIs<ClientMessage.CreateRoom>(decoded)
        assertEquals("Alex", decoded.playerName)
    }

    @Test
    fun `given a client message when encoded then its type is named by the shared discriminator`() {
        // When
        val encoded = json.encodeToString(ClientMessage.serializer(), ClientMessage.CreateRoom("Alex"))

        // Then
        assertTrue(encoded.contains("\"type\":\"CREATE_ROOM\""), encoded)
    }

    @Test
    fun `given play again message when encoded then it round trips`() {
        // Given
        val message: ClientMessage = ClientMessage.PlayAgain("AB12", "player-1")

        // When
        val decoded = json.decodeFromString<ClientMessage>(json.encodeToString(ClientMessage.serializer(), message))

        // Then
        assertEquals(message, decoded)
    }

    @Test
    fun `given start game with one word turns when encoded then the room settings round trip`() {
        // Given
        val message: ClientMessage = ClientMessage.StartGame("AB12", "host", null, RoomSettings(rounds = 4, oneWordPerTurn = true))

        // When
        val decoded = json.decodeFromString<ClientMessage>(json.encodeToString(ClientMessage.serializer(), message))

        // Then
        assertEquals(message, decoded)
    }

    @Test
    fun `given room settings without one word turns when decoded then free writing is assumed`() {
        // When
        val settings = json.decodeFromString<RoomSettings>("""{"rounds":2}""")

        // Then
        assertEquals(RoomSettings(rounds = 2, oneWordPerTurn = false), settings)
    }

    @Test
    fun `given room state message when encoded then it preserves players`() {
        // Given
        val room =
            RoomState(
                "AB12",
                "host",
                listOf(Player("host", "Alex", PlayerColour.Blue)),
                GameStatus.Lobby,
                maxPlayers = 10,
                settings = RoomSettings(rounds = 3),
            )
        val message: ServerMessage = ServerMessage.RoomStateChanged(room)

        // When
        val decoded = json.decodeFromString<ServerMessage>(json.encodeToString(ServerMessage.serializer(), message))

        // Then
        assertEquals(message, decoded)
    }

    @Test
    fun `given a finished game when encoded then it preserves the finished room`() {
        // Given
        val room =
            RoomState(
                "AB12",
                "host",
                listOf(Player("host", "Alex", PlayerColour.Blue)),
                GameStatus.Finished,
                maxPlayers = 10,
                settings = RoomSettings(rounds = 3),
            )
        val message: ServerMessage = ServerMessage.GameFinished(room)

        // When
        val decoded = json.decodeFromString<ServerMessage>(json.encodeToString(ServerMessage.serializer(), message))

        // Then
        assertEquals(message, decoded)
    }
}
