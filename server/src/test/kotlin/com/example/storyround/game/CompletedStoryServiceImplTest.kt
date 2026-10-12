package com.example.storyround.game

import com.example.storyround.identity.IdentifierGenerator
import com.example.storyround.logging.Logger
import com.example.storyround.persistence.StoryRepository
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.SavedStory
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.room.RegistryResult
import com.example.storyround.story.SavedStoryMapper
import com.example.storyround.time.TimeProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CompletedStoryServiceImplTest {
    private val host = Player("host", "Alex", PlayerColour.Blue)
    private val finishedRoom =
        RoomState("AB12", host.id, listOf(host), GameStatus.Finished, prompt = "A door opened.", maxPlayers = 10, maxRounds = 1)
    private val finished = RegistryResult.Success(finishedRoom, null, listOf(ServerMessage.GameFinished(finishedRoom)))
    private val story = SavedStory("story-1", "A door opened.", host.id, 42L, listOf(host), emptyList())
    private val repository = mockk<StoryRepository>()
    private val savedStoryMapper = mockk<SavedStoryMapper> { every { map(finishedRoom, "story-1", 42L) } returns story }
    private val identifierGenerator = mockk<IdentifierGenerator> { every { generate() } returns "story-1" }
    private val timeProvider = mockk<TimeProvider> { every { nowEpochMillis() } returns 42L }
    private val logger = mockk<Logger>(relaxed = true)

    @Test
    fun `given a result that finishes the game when saved then the story is persisted and its id remembered`() =
        runTest {
            every { repository.save(story) } returns Unit
            val service = service(repository)

            val result = service.saveIfFinished(finished)

            assertEquals(StorySaveResult.Saved("story-1"), result)
            assertEquals("story-1", service.savedStoryId("AB12"))
            verify(exactly = 1) { repository.save(story) }
        }

    @Test
    fun `given a result that does not finish the game when saved then nothing is persisted`() =
        runTest {
            val service = service(repository)
            val playing = RegistryResult.Success(finishedRoom.copy(status = GameStatus.Playing), null)

            assertEquals(StorySaveResult.NotSaved, service.saveIfFinished(playing))
            verify(exactly = 0) { repository.save(any()) }
        }

    @Test
    fun `given the server runs without persistence when a game finishes then nothing is saved`() =
        runTest {
            assertEquals(StorySaveResult.NotSaved, service(storyRepository = null).saveIfFinished(finished))
        }

    @Test
    fun `given persistence fails when a game finishes then the failure is logged and reported`() =
        runTest {
            every { repository.save(story) } throws IllegalStateException("Database connection lost")
            val service = service(repository)

            assertEquals(StorySaveResult.Failed, service.saveIfFinished(finished))
            assertNull(service.savedStoryId("AB12"))
            verify { logger.error(any(), any(), match { it.message == "Database connection lost" }) }
        }

    @Test
    fun `given a saved story when the room is forgotten then its id is no longer offered`() =
        runTest {
            every { repository.save(story) } returns Unit
            val service = service(repository)
            service.saveIfFinished(finished)

            service.forget("AB12")

            assertNull(service.savedStoryId("AB12"))
        }

    private fun TestScope.service(storyRepository: StoryRepository?) =
        CompletedStoryServiceImpl(
            storyRepository = storyRepository,
            savedStoryMapper = savedStoryMapper,
            identifierGenerator = identifierGenerator,
            timeProvider = timeProvider,
            ioDispatcher = StandardTestDispatcher(testScheduler),
            logger = logger,
        )
}
