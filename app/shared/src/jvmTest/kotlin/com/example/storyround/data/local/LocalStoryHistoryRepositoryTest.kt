package com.example.storyround.data.local

import com.example.storyround.logging.Logger
import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomSettings
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.SavedStory
import com.example.storyround.story.SavedStoryMapperImpl
import com.russhwolf.settings.MapSettings
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocalStoryHistoryRepositoryTest {
    private val player = Player("player-1", "Alex", PlayerColour.Blue)
    private val logger = mockk<Logger>(relaxed = true)
    private val store = SettingsStoryHistoryStore(MapSettings(), storageJson(), logger)

    @Test
    fun `given stories saved on this device when the repository starts then they are listed`() {
        // Given
        store.save(savedStory("story-1"))

        // When
        val repository = repository()

        // Then
        assertEquals(
            listOf("story-1"),
            repository.state.value.stories
                .map(SavedStory::id),
        )
    }

    @Test
    fun `given a story saved elsewhere when history is loaded then it is listed`() {
        // Given
        val repository = repository()
        store.save(savedStory("story-1"))

        // When
        repository.load()

        // Then
        assertEquals(
            listOf("story-1"),
            repository.state.value.stories
                .map(SavedStory::id),
        )
    }

    @Test
    fun `given a saved story when it is opened and closed then it is selected and released`() {
        // Given
        store.save(savedStory("story-1"))
        val repository = repository()

        // When
        val opened = repository.open("story-1")
        val selectedWhileOpen = repository.state.value.selectedStory
        val openedMissing = repository.open("missing")
        repository.close()

        // Then
        assertTrue(opened)
        assertEquals("story-1", selectedWhileOpen?.id)
        assertFalse(openedMissing)
        assertNull(repository.state.value.selectedStory)
    }

    @Test
    fun `given a finished game when saved then it is stored and listed with the current time`() {
        // Given
        val repository = repository()

        // When
        repository.saveFinished(finishedRoom())

        // Then
        val saved =
            repository.state.value.stories
                .single()
        assertEquals("A door opened.", saved.prompt)
        assertEquals(player.id, saved.promptPlayerId)
        assertEquals(1L, saved.createdAtEpochMillis)
        assertEquals(listOf(saved), store.load())
    }

    @Test
    fun `given the same finished game arrives again after a reconnect when saved then it is kept only once`() {
        // Given
        val repository = repository()

        // When
        repository.saveFinished(finishedRoom())
        repository.saveFinished(finishedRoom())

        // Then
        assertEquals(1, repository.state.value.stories.size)
    }

    @Test
    fun `given a finished game without an opening prompt when saved then nothing is stored and it is logged`() {
        // Given
        val repository = repository()

        // When
        repository.saveFinished(finishedRoom(prompt = null))

        // Then
        assertTrue(
            repository.state.value.stories
                .isEmpty(),
        )
        verify { logger.error(any(), match { it.contains("AB12") && it.contains("no opening prompt") }, null) }
    }

    private fun repository() =
        LocalStoryHistoryRepository(
            storyHistoryStore = store,
            savedStoryMapper = SavedStoryMapperImpl(),
            timeProvider = mockk { every { nowEpochMillis() } returns 1L },
            logger = logger,
        )

    private fun savedStory(id: String) = SavedStory(id, "A door opened.", player.id, 1, listOf(player), emptyList())

    private fun finishedRoom(prompt: String? = "A door opened.") =
        RoomState(
            roomCode = "AB12",
            hostId = player.id,
            players = listOf(player),
            status = GameStatus.Finished,
            openingPlayerId = player.id,
            prompt = prompt,
            contributions = listOf(Contribution(player.id, "The end.", 0)),
            maxPlayers = 10,
            settings = RoomSettings(rounds = 3),
        )
}
