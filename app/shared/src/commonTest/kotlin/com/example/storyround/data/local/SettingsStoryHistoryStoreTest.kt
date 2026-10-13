package com.example.storyround.data.local

import com.example.storyround.protocol.SavedStory
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsStoryHistoryStoreTest {
    private val settings = MapSettings()
    private val store = SettingsStoryHistoryStore(settings)

    @Test
    fun `given stories saved in any order when loaded then the newest comes first`() {
        store.save(story("older", createdAt = 10))
        store.save(story("newer", createdAt = 20))

        assertEquals(listOf("newer", "older"), store.load().map(SavedStory::id))
    }

    @Test
    fun `given a story that is already saved when saved again then the new copy replaces it`() {
        store.save(story("story-1", createdAt = 10, prompt = "First copy."))

        store.save(story("story-1", createdAt = 10, prompt = "Second copy."))

        assertEquals(listOf("Second copy."), store.load().map(SavedStory::prompt))
    }

    @Test
    fun `given unreadable saved data when loaded then history is empty instead of failing`() {
        settings.putString("story_round.local_story_history", "not json")

        assertEquals(emptyList(), store.load())
    }

    private fun story(
        id: String,
        createdAt: Long,
        prompt: String = "A door opened.",
    ) = SavedStory(id, prompt, "player-1", createdAt, emptyList(), emptyList())
}
