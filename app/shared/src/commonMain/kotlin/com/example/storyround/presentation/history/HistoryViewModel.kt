package com.example.storyround.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.repository.StoryHistoryRepository
import com.example.storyround.presentation.WhileUiSubscribed
import com.example.storyround.presentation.mapper.GameErrorUiMapper
import com.example.storyround.presentation.mapper.SavedStoryUiMapper
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

internal class HistoryViewModel(
    gameRepository: GameRepository,
    private val storyHistoryRepository: StoryHistoryRepository,
    private val savedStoryUiMapper: SavedStoryUiMapper,
    private val gameErrorUiMapper: GameErrorUiMapper,
) : ViewModel() {
    val uiState: StateFlow<HistoryUiState> =
        combine(storyHistoryRepository.state, gameRepository.state) { history, state ->
            HistoryUiState.Content(history.stories.map(savedStoryUiMapper::map), state.error?.let(gameErrorUiMapper::map))
        }.stateIn(viewModelScope, WhileUiSubscribed, HistoryUiState.Loading)

    init {
        storyHistoryRepository.load()
    }

    fun onAction(action: HistoryAction) {
        when (action) {
            is HistoryAction.OpenStory -> storyHistoryRepository.open(action.id)
            HistoryAction.Back -> Unit
        }
    }
}
