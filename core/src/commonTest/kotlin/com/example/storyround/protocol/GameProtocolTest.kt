package com.example.storyround.protocol

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GameProtocolTest {
    private val json = Json { classDiscriminator = "type" }

    @Test
    fun `given create room message when encoded then it round trips`() {
        val message: ClientMessage = ClientMessage.CreateRoom("Alex")

        val decoded = json.decodeFromString<ClientMessage>(json.encodeToString(ClientMessage.serializer(), message))

        assertIs<ClientMessage.CreateRoom>(decoded)
        assertEquals("Alex", decoded.playerName)
    }

    @Test
    fun `given play again message when encoded then it round trips`() {
        val message: ClientMessage = ClientMessage.PlayAgain("AB12", "player-1")

        val decoded = json.decodeFromString<ClientMessage>(json.encodeToString(ClientMessage.serializer(), message))

        assertEquals(message, decoded)
    }

    @Test
    fun `given room state message when encoded then it preserves players`() {
        val room = RoomState("AB12", "host", listOf(Player("host", "Alex", PlayerColour.Blue)), GameStatus.Lobby, maxPlayers = 10, maxRounds = 3)
        val message: ServerMessage = ServerMessage.RoomStateChanged(room)

        val decoded = json.decodeFromString<ServerMessage>(json.encodeToString(ServerMessage.serializer(), message))

        assertEquals(message, decoded)
    }

    @Test
    fun `given a finished game when encoded then it preserves the saved story id`() {
        val room = RoomState("AB12", "host", listOf(Player("host", "Alex", PlayerColour.Blue)), GameStatus.Finished, maxPlayers = 10, maxRounds = 3)
        val message: ServerMessage = ServerMessage.GameFinished(room, storyId = "story-1")

        val decoded = json.decodeFromString<ServerMessage>(json.encodeToString(ServerMessage.serializer(), message))

        assertEquals(message, decoded)
    }
}
