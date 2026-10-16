package com.example.storyround.data.local

import com.example.storyround.logging.RecordingLogger
import com.example.storyround.protocol.SavedStory
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsStoryHistoryStoreTest {
    private val settings = MapSettings()
    private val logger = RecordingLogger()
    private val store = SettingsStoryHistoryStore(settings, storageJson(), logger)

    @Test
    fun `given stories saved in any order when loaded then the newest comes first`() {
        // Given
        store.save(story("older", createdAt = 10))
        store.save(story("newer", createdAt = 20))

        // When
        val ids = store.load().map(SavedStory::id)

        // Then
        assertEquals(listOf("newer", "older"), ids)
    }

    @Test
    fun `given a story that is already saved when saved again then the new copy replaces it`() {
        // Given
        store.save(story("story-1", createdAt = 10, prompt = "First copy."))

        // When
        store.save(story("story-1", createdAt = 10, prompt = "Second copy."))

        // Then
        assertEquals(listOf("Second copy."), store.load().map(SavedStory::prompt))
    }

    @Test
    fun `given unreadable saved data when loaded then history is empty instead of failing`() {
        // Given
        settings.putString("story_round.local_story_history", "not json")

        // When
        val history = store.load()

        // Then
        assertEquals(emptyList(), history)
    }

    @Test
    fun `given unreadable saved data when loaded then a warning is logged`() {
        // Given
        settings.putString("story_round.local_story_history", "not json")

        // When
        store.load()

        // Then
        assertEquals(1, logger.warnings.size)
    }

    private fun story(
        id: String,
        createdAt: Long,
        prompt: String = "A door opened.",
    ) = SavedStory(id, prompt, "player-1", createdAt, emptyList(), emptyList())
}
