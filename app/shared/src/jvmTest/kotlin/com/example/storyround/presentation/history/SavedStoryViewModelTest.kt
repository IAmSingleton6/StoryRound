package com.example.storyround.presentation.history

import com.example.storyround.domain.model.RemoteGameState
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.presentation.mapper.ContributionUiMapperImpl
import com.example.storyround.presentation.mapper.PlayerUiMapperImpl
import com.example.storyround.presentation.mapper.SavedStoryUiMapperImpl
import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.SavedStory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SavedStoryViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given a selected saved story when observed then its pieces and every writer's share are exposed`() =
        runTest(dispatcher) {
            val alex = Player("alex", "Alex", PlayerColour.Blue)
            val sam = Player("sam", "Sam", PlayerColour.Yellow)
            val jo = Player("jo", "Jo", PlayerColour.Pink)
            val story =
                SavedStory(
                    id = "story-1",
                    prompt = "A door opened.",
                    promptPlayerId = alex.id,
                    createdAtEpochMillis = 1,
                    players = listOf(alex, sam, jo),
                    contributions = listOf(Contribution(sam.id, "A horse arrived.", 0), Contribution(alex.id, "It wore a hat.", 1)),
                )
            val repository = mockk<GameRepository>(relaxed = true)
            every { repository.state } returns MutableStateFlow(RemoteGameState(selectedStory = story))
            val viewModel = SavedStoryViewModel(repository, SavedStoryUiMapperImpl(PlayerUiMapperImpl(), ContributionUiMapperImpl()))
            val collection = backgroundScope.launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            assertEquals(listOf("A door opened.", "A horse arrived.", "It wore a hat."), uiState.segments.map { it.text })
            assertEquals(listOf("Alex" to 2, "Sam" to 1, "Jo" to 0), uiState.writers.map { it.player.name to it.pieces })
            collection.cancel()
        }
}
