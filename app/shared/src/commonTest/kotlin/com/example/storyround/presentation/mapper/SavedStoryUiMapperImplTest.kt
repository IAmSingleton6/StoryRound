package com.example.storyround.presentation.mapper

import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.SavedStory
import kotlin.test.Test
import kotlin.test.assertEquals

class SavedStoryUiMapperImplTest {
    private val mapper = SavedStoryUiMapperImpl(PlayerUiMapperImpl(), ContributionUiMapperImpl(), StorySegmentMapperImpl())

    @Test
    fun `given a saved story with a departed player's contribution when mapped then that contribution is omitted`() {
        val story =
            SavedStory(
                id = "story-1",
                prompt = "Prompt",
                promptPlayerId = "alex",
                createdAtEpochMillis = 1,
                players = listOf(Player("alex", "Alex", PlayerColour.Blue)),
                contributions = listOf(Contribution("unknown", "Ignored", turnNumber = 1)),
            )

        val mapped = mapper.map(story)

        assertEquals(emptyList(), mapped.contributions)
        assertEquals("Alex", mapped.promptAuthor?.name)
        assertEquals(listOf("Prompt"), mapped.segments.map { it.text })
        assertEquals(listOf("Alex" to 1), mapped.writers.map { it.player.name to it.pieces })
    }
}
