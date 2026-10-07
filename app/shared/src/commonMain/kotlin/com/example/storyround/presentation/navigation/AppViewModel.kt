package com.example.storyround.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.usecase.ConnectToGameUseCase
import com.example.storyround.protocol.GameStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal enum class AppDestination { Home, Lobby, Story, Finished, History, SavedStory }

private enum class ManualDestination { Home, History }

internal class AppViewModel(
    private val repository: GameRepository,
    connectToGame: ConnectToGameUseCase,
) : ViewModel() {
    private val manualDestination = MutableStateFlow(ManualDestination.Home)

    val destination: StateFlow<AppDestination> =
        combine(repository.state, manualDestination) { state, manual ->
            when {
                state.selectedStory != null -> AppDestination.SavedStory
                manual == ManualDestination.History -> AppDestination.History
                state.room == null -> AppDestination.Home
                state.room.status == GameStatus.Lobby -> AppDestination.Lobby
                state.room.status == GameStatus.Prompt || state.room.status == GameStatus.Playing -> AppDestination.Story
                state.room.status == GameStatus.Finished -> AppDestination.Finished
                else -> AppDestination.Home
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppDestination.Home)

    init {
        viewModelScope.launch { connectToGame() }
    }

    fun showHistory() {
        manualDestination.value = ManualDestination.History
    }

    fun showRoom() {
        manualDestination.value = ManualDestination.Home
    }

    fun returnToHistory() {
        repository.closeStory()
        showHistory()
    }

    fun returnHome() {
        manualDestination.value = ManualDestination.Home
        repository.disconnect()
    }

    override fun onCleared() {
        repository.close()
    }
}
