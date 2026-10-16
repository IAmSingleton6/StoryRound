package com.example.storyround.data.local

import com.example.storyround.logging.Logger
import com.example.storyround.protocol.SavedStory
import com.russhwolf.settings.Settings
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private const val LOG_TAG = "SettingsStoryHistoryStore"

internal class SettingsStoryHistoryStore(
    private val settings: Settings,
    private val json: Json,
    private val logger: Logger,
) : StoryHistoryStore {
    private val serializer = ListSerializer(SavedStory.serializer())

    override fun load(): List<SavedStory> =
        settings
            .getStringOrNull(STORY_HISTORY_KEY)
            ?.let(::decode)
            .orEmpty()
            .sortedByDescending(SavedStory::createdAtEpochMillis)

    override fun save(story: SavedStory) {
        val stories = load().filterNot { it.id == story.id }
        settings.putString(STORY_HISTORY_KEY, json.encodeToString(serializer, listOf(story) + stories))
    }

    private fun decode(serialized: String): List<SavedStory>? =
        runCatching { json.decodeFromString(serializer, serialized) }
            .onFailure { logger.warn(LOG_TAG, "Ignored unreadable saved story history (${it::class.simpleName}).") }
            .getOrNull()

    private companion object {
        const val STORY_HISTORY_KEY = "story_round.local_story_history"
    }
}
