package com.example.storyround.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.usecase.LoadStoryHistoryUseCase
import com.example.storyround.domain.usecase.OpenSavedStoryUseCase
import com.example.storyround.presentation.mapper.SavedStoryUiMapper
import com.example.storyround.presentation.model.toHistoryStory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class HistoryViewModel(
    repository: GameRepository,
    private val loadStoryHistory: LoadStoryHistoryUseCase,
    private val openSavedStory: OpenSavedStoryUseCase,
    private val savedStoryUiMapper: SavedStoryUiMapper,
) : ViewModel() {
    val uiState: StateFlow<HistoryUiState> =
        repository.state
            .map { state ->
                when {
                    state.isLoadingHistory -> HistoryUiState.Loading
                    state.error != null -> HistoryUiState.Error(state.error)
                    else -> HistoryUiState.Content(state.history.map { savedStoryUiMapper.map(it).toHistoryStory() })
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState.Loading)

    init {
        viewModelScope.launch { loadStoryHistory() }
    }

    fun onAction(action: HistoryAction) {
        if (action is HistoryAction.OpenStory) viewModelScope.launch { openSavedStory(action.id) }
    }
}
