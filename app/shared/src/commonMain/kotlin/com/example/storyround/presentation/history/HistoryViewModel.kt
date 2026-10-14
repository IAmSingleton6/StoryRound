package com.example.storyround.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.usecase.LoadStoryHistoryUseCase
import com.example.storyround.domain.usecase.OpenSavedStoryUseCase
import com.example.storyround.presentation.WhileUiSubscribed
import com.example.storyround.presentation.mapper.SavedStoryUiMapper
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class HistoryViewModel(
    repository: GameRepository,
    private val loadStoryHistoryUseCase: LoadStoryHistoryUseCase,
    private val openSavedStoryUseCase: OpenSavedStoryUseCase,
    private val savedStoryUiMapper: SavedStoryUiMapper,
) : ViewModel() {
    val uiState: StateFlow<HistoryUiState> =
        repository.state
            .map { state -> HistoryUiState.Content(state.history.map(savedStoryUiMapper::map), state.error) }
            .stateIn(viewModelScope, WhileUiSubscribed, HistoryUiState.Loading)

    init {
        viewModelScope.launch { loadStoryHistoryUseCase() }
    }

    fun onAction(action: HistoryAction) {
        when (action) {
            is HistoryAction.OpenStory -> viewModelScope.launch { openSavedStoryUseCase(action.id) }
            HistoryAction.Back -> Unit
        }
    }
}
