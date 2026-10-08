package com.example.storyround.room

import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import kotlin.test.Test
import kotlin.test.assertEquals

class RoomStateMapperImplTest {
    private val mapper = RoomStateMapperImpl()

    @Test
    fun `given populated mutable room when mapped then it exposes the complete room state as snapshots`() {
        val host = Player(id = "host", name = "Alex", colour = PlayerColour.Blue)
        val contribution = Contribution(playerId = "host", text = "Once upon a time", turnNumber = 2)
        val room =
            MutableGameRoom(
                code = "AB12",
                hostId = host.id,
                players = mutableListOf(host),
                maxPlayers = 6,
                status = GameStatus.Playing,
                prompt = "A strange door appears.",
                openingPlayerId = host.id,
                contributions = mutableListOf(contribution),
                currentPlayerId = host.id,
                turnNumber = 2,
                turnDeadlineEpochMillis = 1_234L,
                maxRounds = 4,
            )

        val state = mapper.map(room)

        assertEquals("AB12", state.roomCode)
        assertEquals(host.id, state.hostId)
        assertEquals(listOf(host), state.players)
        assertEquals(GameStatus.Playing, state.status)
        assertEquals(host.id, state.openingPlayerId)
        assertEquals("A strange door appears.", state.prompt)
        assertEquals(listOf(contribution), state.contributions)
        assertEquals(host.id, state.currentPlayerId)
        assertEquals(2, state.turnNumber)
        assertEquals(1_234L, state.turnDeadlineEpochMillis)
        assertEquals(6, state.maxPlayers)
        assertEquals(4, state.maxRounds)

        room.players.clear()
        room.contributions.clear()

        assertEquals(listOf(host), state.players)
        assertEquals(listOf(contribution), state.contributions)
    }
}
