package com.example.storyround.data.local

import com.example.storyround.protocol.SavedStory
import com.russhwolf.settings.Settings
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

internal class SettingsStoryHistoryStore(
    private val settings: Settings,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : StoryHistoryStore {
    override fun load(): List<SavedStory> =
        settings
            .getStringOrNull(STORY_HISTORY_KEY)
            ?.let { serialized -> runCatching { json.decodeFromString(ListSerializer(SavedStory.serializer()), serialized) }.getOrNull() }
            .orEmpty()
            .sortedByDescending(SavedStory::createdAtEpochMillis)

    override fun save(story: SavedStory) {
        val stories = load().filterNot { it.id == story.id }
        settings.putString(STORY_HISTORY_KEY, json.encodeToString(ListSerializer(SavedStory.serializer()), listOf(story) + stories))
    }

    private companion object {
        const val STORY_HISTORY_KEY = "story_round.local_story_history"
    }
}
