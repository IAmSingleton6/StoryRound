package com.example.storyround.data.local

import com.example.storyround.protocol.SavedStory
import com.russhwolf.settings.Settings
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

internal class SettingsStoryHistoryStore(
    private val settings: Settings,
) : StoryHistoryStore {
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(SavedStory.serializer())

    override fun load(): List<SavedStory> =
        settings
            .getStringOrNull(STORY_HISTORY_KEY)
            ?.let { serialized -> runCatching { json.decodeFromString(serializer, serialized) }.getOrNull() }
            .orEmpty()
            .sortedByDescending(SavedStory::createdAtEpochMillis)

    override fun save(story: SavedStory) {
        val stories = load().filterNot { it.id == story.id }
        settings.putString(STORY_HISTORY_KEY, json.encodeToString(serializer, listOf(story) + stories))
    }

    private companion object {
        const val STORY_HISTORY_KEY = "story_round.local_story_history"
    }
}
