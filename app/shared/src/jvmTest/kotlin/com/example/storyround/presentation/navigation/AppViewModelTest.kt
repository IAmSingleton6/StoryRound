package com.example.storyround.presentation.navigation

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.storyround.domain.model.GameState
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.usecase.CloseSavedStoryUseCase
import com.example.storyround.domain.usecase.ConnectToGameUseCase
import com.example.storyround.domain.usecase.DisconnectFromGameUseCase
import com.example.storyround.domain.usecase.LeaveRoomUseCase
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.RoomSettings
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.SavedStory
import io.mockk.coVerify
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val gameState = MutableStateFlow(GameState())
    private val repository = mockk<GameRepository> { every { state } returns gameState }
    private val connectToGameUseCase = mockk<ConnectToGameUseCase>(relaxed = true)
    private val leaveRoomUseCase = mockk<LeaveRoomUseCase>(relaxed = true)
    private val disconnectFromGameUseCase = mockk<DisconnectFromGameUseCase>(relaxed = true)
    private val closeSavedStoryUseCase = mockk<CloseSavedStoryUseCase>(relaxed = true)
    private val story = SavedStory("story-1", "A door opened.", "host", 1, emptyList(), emptyList())

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given the app starts when the view model is created then it connects to the game`() =
        runTest(dispatcher) {
            viewModel()
            advanceUntilIdle()

            coVerify(exactly = 1) { connectToGameUseCase() }
        }

    @Test
    fun `given each room status when observed then the matching screen is shown`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            val expected =
                mapOf(
                    null to AppDestination.Home,
                    GameStatus.Lobby to AppDestination.Lobby,
                    GameStatus.Prompt to AppDestination.Story,
                    GameStatus.Playing to AppDestination.Story,
                    GameStatus.Finished to AppDestination.Finished,
                )

            expected.forEach { (status, destination) ->
                gameState.value = GameState(room = status?.let(::room))
                advanceUntilIdle()
                assertEquals(destination, viewModel.destination.value, "Destination for $status")
            }
        }

    @Test
    fun `given a room when history is opened and closed then history shows and back returns to the room`() =
        runTest(dispatcher) {
            gameState.value = GameState(room = room(GameStatus.Lobby))
            val viewModel = viewModel()

            viewModel.showHistory()
            advanceUntilIdle()
            assertEquals(AppDestination.History, viewModel.destination.value)

            viewModel.closeHistory()
            advanceUntilIdle()
            assertEquals(AppDestination.Lobby, viewModel.destination.value)
        }

    @Test
    fun `given a selected saved story when it is closed then the story is released and history reopens`() =
        runTest(dispatcher) {
            gameState.value = GameState(selectedStory = story)
            val viewModel = viewModel()
            advanceUntilIdle()
            assertEquals(AppDestination.SavedStory, viewModel.destination.value)

            viewModel.closeSavedStory()
            gameState.value = GameState()
            advanceUntilIdle()

            verify(exactly = 1) { closeSavedStoryUseCase() }
            assertEquals(AppDestination.History, viewModel.destination.value)
        }

    @Test
    fun `given a finished game when returning home then the game is disconnected without leaving`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            viewModel.returnHome()
            advanceUntilIdle()

            verify(exactly = 1) { disconnectFromGameUseCase() }
            coVerify(exactly = 0) { leaveRoomUseCase() }
        }

    @Test
    fun `given the lobby when leaving then the room is left`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            viewModel.leaveLobby()
            advanceUntilIdle()

            coVerify(exactly = 1) { leaveRoomUseCase() }
        }

    @Test
    fun `given the app UI goes away when the view model is cleared then the game is disconnected but the shared repository stays usable`() =
        runTest(dispatcher) {
            val store = ViewModelStore()
            ViewModelProvider.create(
                store,
                viewModelFactory {
                    initializer {
                        AppViewModel(repository, connectToGameUseCase, leaveRoomUseCase, disconnectFromGameUseCase, closeSavedStoryUseCase)
                    }
                },
            )[AppViewModel::class]
            advanceUntilIdle()

            store.clear()

            verify(exactly = 1) { disconnectFromGameUseCase() }
            coVerify(exactly = 0) { leaveRoomUseCase() }
        }

    private fun TestScope.viewModel(): AppViewModel {
        val viewModel =
            AppViewModel(repository, connectToGameUseCase, leaveRoomUseCase, disconnectFromGameUseCase, closeSavedStoryUseCase)
        backgroundScope.launch { viewModel.destination.collect() }
        return viewModel
    }

    private fun room(status: GameStatus) =
        RoomState("AB12", "host", emptyList(), status, maxPlayers = 10, settings = RoomSettings(rounds = 3))
}
