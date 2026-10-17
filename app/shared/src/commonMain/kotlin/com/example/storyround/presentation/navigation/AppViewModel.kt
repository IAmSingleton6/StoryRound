package com.example.storyround.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyround.domain.model.GameState
import com.example.storyround.domain.model.StoryHistoryState
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.repository.StoryHistoryRepository
import com.example.storyround.presentation.WhileUiSubscribed
import com.example.storyround.protocol.GameStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal enum class AppDestination { Home, Lobby, Story, Finished, History, SavedStory }

internal class AppViewModel(
    private val gameRepository: GameRepository,
    private val storyHistoryRepository: StoryHistoryRepository,
) : ViewModel() {
    private val isHistoryOpen = MutableStateFlow(false)

    val destination: StateFlow<AppDestination> =
        combine(gameRepository.state, storyHistoryRepository.state, isHistoryOpen, ::destinationFor)
            .stateIn(viewModelScope, WhileUiSubscribed, AppDestination.Home)

    init {
        viewModelScope.launch { gameRepository.connect() }
    }

    fun showHistory() {
        isHistoryOpen.value = true
    }

    fun closeHistory() {
        isHistoryOpen.value = false
    }

    fun closeSavedStory() {
        storyHistoryRepository.close()
        showHistory()
    }

    fun goHomeWithoutLeavingRoom() {
        isHistoryOpen.value = false
        gameRepository.disconnect()
    }

    fun leaveLobby() {
        isHistoryOpen.value = false
        viewModelScope.launch { gameRepository.leaveRoom() }
    }

    override fun onCleared() {
        gameRepository.disconnect()
    }

    private fun destinationFor(
        state: GameState,
        history: StoryHistoryState,
        isHistoryOpen: Boolean,
    ): AppDestination {
        val room = state.room
        return when {
            history.selectedStory != null -> AppDestination.SavedStory
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
