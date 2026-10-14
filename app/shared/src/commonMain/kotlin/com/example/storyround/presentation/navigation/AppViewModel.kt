package com.example.storyround.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyround.domain.model.GameState
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.usecase.CloseSavedStoryUseCase
import com.example.storyround.domain.usecase.ConnectToGameUseCase
import com.example.storyround.domain.usecase.DisconnectFromGameUseCase
import com.example.storyround.domain.usecase.LeaveRoomUseCase
import com.example.storyround.presentation.WhileUiSubscribed
import com.example.storyround.protocol.GameStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal enum class AppDestination { Home, Lobby, Story, Finished, History, SavedStory }

internal class AppViewModel(
    repository: GameRepository,
    connectToGameUseCase: ConnectToGameUseCase,
    private val leaveRoomUseCase: LeaveRoomUseCase,
    private val disconnectFromGameUseCase: DisconnectFromGameUseCase,
    private val closeSavedStoryUseCase: CloseSavedStoryUseCase,
) : ViewModel() {
    private val isHistoryOpen = MutableStateFlow(false)

    val destination: StateFlow<AppDestination> =
        combine(repository.state, isHistoryOpen, ::destinationFor)
            .stateIn(viewModelScope, WhileUiSubscribed, AppDestination.Home)

    init {
        viewModelScope.launch { connectToGameUseCase() }
    }

    fun showHistory() {
        isHistoryOpen.value = true
    }

    fun closeHistory() {
        isHistoryOpen.value = false
    }

    fun closeSavedStory() {
        closeSavedStoryUseCase()
        showHistory()
    }

    fun returnHome() {
        isHistoryOpen.value = false
        disconnectFromGameUseCase()
    }

    fun leaveLobby() {
        isHistoryOpen.value = false
        viewModelScope.launch { leaveRoomUseCase() }
    }

    override fun onCleared() {
        disconnectFromGameUseCase()
    }

    private fun destinationFor(
        state: GameState,
        isHistoryOpen: Boolean,
    ): AppDestination {
        val room = state.room
        return when {
            state.selectedStory != null -> AppDestination.SavedStory
            isHistoryOpen -> AppDestination.History
            room == null -> AppDestination.Home
            else ->
                when (room.status) {
                    GameStatus.Lobby -> AppDestination.Lobby
                    GameStatus.Prompt, GameStatus.Playing -> AppDestination.Story
                    GameStatus.Finished -> AppDestination.Finished
                }
        }
    }
}
