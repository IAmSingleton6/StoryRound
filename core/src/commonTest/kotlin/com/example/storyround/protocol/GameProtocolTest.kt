package com.example.storyround.protocol

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GameProtocolTest {
    private val json = protocolJson()

    @Test
    fun `given create room message when encoded then it round trips`() {
        val message: ClientMessage = ClientMessage.CreateRoom("Alex")

        val decoded = json.decodeFromString<ClientMessage>(json.encodeToString(ClientMessage.serializer(), message))

        assertIs<ClientMessage.CreateRoom>(decoded)
        assertEquals("Alex", decoded.playerName)
    }

    @Test
    fun `given a client message when encoded then its type is named by the shared discriminator`() {
        val encoded = json.encodeToString(ClientMessage.serializer(), ClientMessage.CreateRoom("Alex"))

        assertTrue(encoded.contains("\"type\":\"CREATE_ROOM\""), encoded)
    }

    @Test
    fun `given play again message when encoded then it round trips`() {
        val message: ClientMessage = ClientMessage.PlayAgain("AB12", "player-1")

        val decoded = json.decodeFromString<ClientMessage>(json.encodeToString(ClientMessage.serializer(), message))

        assertEquals(message, decoded)
    }

    @Test
    fun `given room state message when encoded then it preserves players`() {
        val room =
            RoomState("AB12", "host", listOf(Player("host", "Alex", PlayerColour.Blue)), GameStatus.Lobby, maxPlayers = 10, maxRounds = 3)
        val message: ServerMessage = ServerMessage.RoomStateChanged(room)

        val decoded = json.decodeFromString<ServerMessage>(json.encodeToString(ServerMessage.serializer(), message))

        assertEquals(message, decoded)
    }

    @Test
    fun `given a finished game when encoded then it preserves the saved story id`() {
        val room =
            RoomState(
                "AB12",
                "host",
                listOf(Player("host", "Alex", PlayerColour.Blue)),
                GameStatus.Finished,
                maxPlayers = 10,
                maxRounds = 3,
            )
        val message: ServerMessage = ServerMessage.GameFinished(room, storyId = "story-1")

        val decoded = json.decodeFromString<ServerMessage>(json.encodeToString(ServerMessage.serializer(), message))

        assertEquals(message, decoded)
    }
}
