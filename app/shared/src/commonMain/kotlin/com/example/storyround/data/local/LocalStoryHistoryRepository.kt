package com.example.storyround.data.local

import com.example.storyround.domain.model.StoryHistoryState
import com.example.storyround.domain.repository.StoryHistoryRepository
import com.example.storyround.logging.Logger
import com.example.storyround.protocol.RoomState
import com.example.storyround.story.SavedStoryMapper
import com.example.storyround.story.SavedStoryMapping
import com.example.storyround.time.TimeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val LOG_TAG = "LocalStoryHistoryRepository"

internal class LocalStoryHistoryRepository(
    private val storyHistoryStore: StoryHistoryStore,
    private val savedStoryMapper: SavedStoryMapper,
    private val timeProvider: TimeProvider,
    private val logger: Logger,
) : StoryHistoryRepository {
    private val mutableState = MutableStateFlow(StoryHistoryState(stories = storyHistoryStore.load()))

    override val state: StateFlow<StoryHistoryState> = mutableState.asStateFlow()

    override fun load() {
        val stories = storyHistoryStore.load()
        mutableState.update { it.copy(stories = stories) }
    }

    override fun open(id: String): Boolean {
        val story = storyHistoryStore.load().firstOrNull { it.id == id } ?: return false
        mutableState.update { it.copy(selectedStory = story) }
        return true
    }

    override fun close() {
        mutableState.update { it.copy(selectedStory = null) }
    }

    override fun saveFinished(room: RoomState) {
        when (val mapping = savedStoryMapper.map(room, localStoryId(room), timeProvider.nowEpochMillis())) {
            is SavedStoryMapping.Mapped -> {
                storyHistoryStore.save(mapping.story)
                load()
            }
            is SavedStoryMapping.Incomplete ->
                logger.error(LOG_TAG, "Not saving finished room ${room.roomCode}: ${mapping.reason}.")
        }
    }

    private fun localStoryId(room: RoomState): String {
        val contributions = room.contributions.joinToString { "${it.playerId}:${it.turnNumber}:${it.text}" }
        return "local-${"${room.roomCode}|${room.prompt}|$contributions".hashCode()}"
    }
}
