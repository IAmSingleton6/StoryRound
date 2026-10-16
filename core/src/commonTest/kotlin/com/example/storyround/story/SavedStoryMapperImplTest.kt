package com.example.storyround.story

import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomSettings
import com.example.storyround.protocol.RoomState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

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
            settings = RoomSettings(rounds = 1),
        )

    @Test
    fun `should keep the writers and the contribution order when a finished room is mapped`() {
        // Given
        val room = finishedRoom

        // When
        val mapping = mapper.map(room, id = "story-1", createdAtEpochMillis = 42L)

        // Then
        val story = assertIs<SavedStoryMapping.Mapped>(mapping).story
        assertEquals("story-1", story.id)
        assertEquals("A door opened.", story.prompt)
        assertEquals(sam.id, story.promptPlayerId)
        assertEquals(42L, story.createdAtEpochMillis)
        assertEquals(listOf(alex, sam), story.players)
        assertEquals(listOf("A horse arrived.", "It wore a hat."), story.contributions.map { it.text })
    }

    @Test
    fun `given a finished room without a recorded opener when mapped then the host is credited with the prompt`() {
        // When
        val mapping = mapper.map(finishedRoom.copy(openingPlayerId = null), id = "story-1", createdAtEpochMillis = 1L)

        // Then
        assertEquals(alex.id, assertIs<SavedStoryMapping.Mapped>(mapping).story.promptPlayerId)
    }

    @Test
    fun `should report an incomplete story when the room has not finished`() {
        // When
        val mapping = mapper.map(finishedRoom.copy(status = GameStatus.Playing), id = "story-1", createdAtEpochMillis = 1L)

        // Then
        assertEquals(SavedStoryMapping.Incomplete("the room has not finished"), mapping)
    }

    @Test
    fun `should report an incomplete story when a finished room has no opening prompt`() {
        // When
        val mapping = mapper.map(finishedRoom.copy(prompt = null), id = "story-1", createdAtEpochMillis = 1L)

        // Then
        assertEquals(SavedStoryMapping.Incomplete("the room has no opening prompt"), mapping)
    }
}
