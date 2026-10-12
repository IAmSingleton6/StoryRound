package com.example.storyround.story

import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SavedStoryMapperImplTest {
    private val mapper = SavedStoryMapperImpl()
    private val alex = Player("alex", "Alex", PlayerColour.Blue)
    private val sam = Player("sam", "Sam", PlayerColour.Green)
    private val finishedRoom =
        RoomState(
            roomCode = "AB12",
            hostId = alex.id,
            players = listOf(alex, sam),
            status = GameStatus.Finished,
            openingPlayerId = sam.id,
            prompt = "A door opened.",
            contributions = listOf(Contribution(alex.id, "A horse arrived.", 0), Contribution(sam.id, "It wore a hat.", 1)),
            maxPlayers = 10,
            maxRounds = 1,
        )

    @Test
    fun `given a finished room when mapped then the story keeps its writers, colours and contribution order`() {
        val story = mapper.map(finishedRoom, id = "story-1", createdAtEpochMillis = 42L)

        assertEquals("story-1", story.id)
        assertEquals("A door opened.", story.prompt)
        assertEquals(sam.id, story.promptPlayerId)
        assertEquals(42L, story.createdAtEpochMillis)
        assertEquals(listOf(alex, sam), story.players)
        assertEquals(listOf("A horse arrived.", "It wore a hat."), story.contributions.map { it.text })
    }

    @Test
    fun `given a finished room without a recorded opener when mapped then the host is credited with the prompt`() {
        val story = mapper.map(finishedRoom.copy(openingPlayerId = null), id = "story-1", createdAtEpochMillis = 1L)

        assertEquals(alex.id, story.promptPlayerId)
    }

    @Test
    fun `given a room that has not finished when mapped then it is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            mapper.map(finishedRoom.copy(status = GameStatus.Playing), id = "story-1", createdAtEpochMillis = 1L)
        }
    }
}
