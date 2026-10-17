package com.example.storyround.presentation.history

import com.example.storyround.domain.model.GameError
import com.example.storyround.domain.model.GameState
import com.example.storyround.domain.model.StoryHistoryState
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.repository.StoryHistoryRepository
import com.example.storyround.presentation.mapper.ContributionUiMapperImpl
import com.example.storyround.presentation.mapper.GameErrorUiMapperImpl
import com.example.storyround.presentation.mapper.PlayerUiMapperImpl
import com.example.storyround.presentation.mapper.SavedStoryUiMapperImpl
import com.example.storyround.presentation.mapper.StorySegmentMapperImpl
import com.example.storyround.presentation.model.UiText
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.SavedStory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.error_reconnecting
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val host = Player("host", "Alex", PlayerColour.Blue)
    private val story = SavedStory("story-1", "A door opened.", host.id, 1, listOf(host), emptyList())
    private val gameState = MutableStateFlow(GameState())
    private val gameRepository = mockk<GameRepository> { every { state } returns gameState }
    private val storyHistoryRepository =
        mockk<StoryHistoryRepository>(relaxed = true) { every { state } returns MutableStateFlow(StoryHistoryState(listOf(story))) }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given saved stories when history opens then they are loaded and shown with each writer's share`() =
        runTest(dispatcher) {
            // When
            val viewModel = viewModel()
            advanceUntilIdle()

            // Then
            val content = assertIs<HistoryUiState.Content>(viewModel.uiState.value)
            assertEquals(listOf("story-1"), content.stories.map { it.id })
            assertEquals(
                listOf(host.name to 1),
                content.stories
                    .single()
                    .writers
                    .map { it.player.name to it.pieces },
            )
            verify(exactly = 1) { storyHistoryRepository.load() }
        }

    @Test
    fun `given a story card when it is opened then that saved story is selected`() =
        runTest(dispatcher) {
            // Given
            val viewModel = viewModel()

            // When
            viewModel.onAction(HistoryAction.OpenStory(story.id))
            advanceUntilIdle()

            // Then
            verify(exactly = 1) { storyHistoryRepository.open(story.id) }
        }

    @Test
    fun `given the connection reports an error when history is shown then the saved stories stay readable`() =
        runTest(dispatcher) {
            // Given
            val viewModel = viewModel()

            // When
            gameState.value = gameState.value.copy(error = GameError.Reconnecting)
            advanceUntilIdle()

            // Then
            val content = assertIs<HistoryUiState.Content>(viewModel.uiState.value)
            assertEquals(UiText.Resource(Res.string.error_reconnecting), content.error)
            assertEquals(listOf("story-1"), content.stories.map { it.id })
        }

    private fun TestScope.viewModel(): HistoryViewModel {
        val viewModel =
            HistoryViewModel(
                gameRepository = gameRepository,
                storyHistoryRepository = storyHistoryRepository,
                savedStoryUiMapper = SavedStoryUiMapperImpl(PlayerUiMapperImpl(), ContributionUiMapperImpl(), StorySegmentMapperImpl()),
                gameErrorUiMapper = GameErrorUiMapperImpl(),
            )
        backgroundScope.launch { viewModel.uiState.collect() }
        return viewModel
    }
}
