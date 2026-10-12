package com.example.storyround.game

import com.example.storyround.identity.IdentifierGenerator
import com.example.storyround.logging.Logger
import com.example.storyround.persistence.StoryRepository
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.room.RegistryResult
import com.example.storyround.story.SavedStoryMapper
import com.example.storyround.time.TimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

private const val LOG_TAG = "CompletedStoryService"

internal class CompletedStoryServiceImpl(
    private val storyRepository: StoryRepository?,
    private val savedStoryMapper: SavedStoryMapper,
    private val identifierGenerator: IdentifierGenerator,
    private val timeProvider: TimeProvider,
    private val ioDispatcher: CoroutineDispatcher,
    private val logger: Logger,
) : CompletedStoryService {
    private val savedStoryIds = ConcurrentHashMap<String, String>()

    override suspend fun saveIfFinished(result: RegistryResult.Success): StorySaveResult {
        val room = result.room
        if (room.status != GameStatus.Finished || result.events.none { it is ServerMessage.GameFinished }) {
            return StorySaveResult.NotSaved
        }
        val repository = storyRepository ?: return StorySaveResult.NotSaved
        val story = savedStoryMapper.map(room, identifierGenerator.generate(), timeProvider.nowEpochMillis())
        return runCatching { withContext(ioDispatcher) { repository.save(story) } }.fold(
            onSuccess = {
                savedStoryIds[room.roomCode] = story.id
                StorySaveResult.Saved(story.id)
            },
            onFailure = { failure ->
                logger.error(LOG_TAG, "Could not save finished story for room ${room.roomCode}.", failure)
                StorySaveResult.Failed
            },
        )
    }

    override fun savedStoryId(roomCode: String): String? = savedStoryIds[roomCode]

    override fun forget(roomCode: String) {
        savedStoryIds.remove(roomCode)
    }
}
