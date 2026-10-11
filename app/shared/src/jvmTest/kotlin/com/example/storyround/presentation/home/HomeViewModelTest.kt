package com.example.storyround.presentation.home

import com.example.storyround.domain.model.RemoteGameState
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.usecase.CreateRoomUseCaseImpl
import com.example.storyround.domain.usecase.JoinRoomUseCaseImpl
import com.example.storyround.protocol.DefaultUserTextSanitizer
import com.example.storyround.protocol.SavedStory
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun `given a name when creating a room then state updates and create command is delegated`() =
        runTest(dispatcher) {
            val remoteState = MutableStateFlow(RemoteGameState())
            val repository = mockk<GameRepository>(relaxed = true)
            every { repository.state } returns remoteState
            val viewModel =
                HomeViewModel(
                    repository = repository,
                    createRoomUseCase = CreateRoomUseCaseImpl(repository),
                    joinRoomUseCase = JoinRoomUseCaseImpl(repository),
                    userTextSanitizer = DefaultUserTextSanitizer(),
                )
            val collection = backgroundScope.launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            viewModel.onAction(HomeAction.PlayerNameChanged("Alex"))
            viewModel.onAction(HomeAction.CreateGame)
            advanceUntilIdle()

            assertEquals("Alex", viewModel.uiState.value.playerName)
            coVerify(exactly = 1) { repository.createRoom("Alex") }
            collection.cancel()
        }

    @Test
    fun `given a name shorter than the minimum after sanitising when entered then create is disabled`() =
        runTest(dispatcher) {
            val repository = mockk<GameRepository>(relaxed = true)
            every { repository.state } returns MutableStateFlow(RemoteGameState())
            val viewModel =
                HomeViewModel(
                    repository = repository,
                    createRoomUseCase = CreateRoomUseCaseImpl(repository),
                    joinRoomUseCase = JoinRoomUseCaseImpl(repository),
                    userTextSanitizer = DefaultUserTextSanitizer(),
                )
            val collection = backgroundScope.launch { viewModel.uiState.collect() }

            viewModel.onAction(HomeAction.PlayerNameChanged(" A "))
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.canCreateGame)

            viewModel.onAction(HomeAction.PlayerNameChanged("Al"))
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.canCreateGame)
            collection.cancel()
        }

    @Test
    fun `given a join code and repository error when state changes then UI exposes both`() =
        runTest(dispatcher) {
            val remoteState = MutableStateFlow(RemoteGameState())
            val repository = mockk<GameRepository>(relaxed = true)
            every { repository.state } returns remoteState
            val viewModel =
                HomeViewModel(
                    repository = repository,
                    createRoomUseCase = CreateRoomUseCaseImpl(repository),
                    joinRoomUseCase = JoinRoomUseCaseImpl(repository),
                    userTextSanitizer = DefaultUserTextSanitizer(),
                )
            val collection = backgroundScope.launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            viewModel.onAction(HomeAction.PlayerNameChanged("Sam"))
            viewModel.onAction(HomeAction.RoomCodeChanged("ab12"))
            remoteState.value = RemoteGameState(error = "That room does not exist.")
            advanceUntilIdle()

            assertEquals("AB12", viewModel.uiState.value.roomCode)
            assertEquals("That room does not exist.", viewModel.uiState.value.error)
            collection.cancel()
        }

    @Test
    fun `given saved stories in repository state when home is observed then the history count is exposed`() =
        runTest(dispatcher) {
            val remoteState = MutableStateFlow(RemoteGameState())
            val repository = mockk<GameRepository>(relaxed = true)
            every { repository.state } returns remoteState
            val viewModel =
                HomeViewModel(
                    repository = repository,
                    createRoomUseCase = CreateRoomUseCaseImpl(repository),
                    joinRoomUseCase = JoinRoomUseCaseImpl(repository),
                    userTextSanitizer = DefaultUserTextSanitizer(),
                )
            val collection = backgroundScope.launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            remoteState.value =
                RemoteGameState(
                    history =
                        listOf(
                            SavedStory("story-1", "A door opened.", "player-1", 1, emptyList(), emptyList()),
                            SavedStory("story-2", "A horse arrived.", "player-1", 2, emptyList(), emptyList()),
                        ),
                )
            advanceUntilIdle()

            assertEquals(2, viewModel.uiState.value.historyCount)
            collection.cancel()
        }
}
