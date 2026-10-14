package com.example.storyround.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.presentation.WhileUiSubscribed
import com.example.storyround.presentation.mapper.SavedStoryUiMapper
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class SavedStoryViewModel(
    repository: GameRepository,
    private val savedStoryUiMapper: SavedStoryUiMapper,
) : ViewModel() {
    val uiState: StateFlow<SavedStoryUiState> =
        repository.state
            .map { state ->
                val story = state.selectedStory?.let(savedStoryUiMapper::map) ?: return@map SavedStoryUiState()
                SavedStoryUiState(segments = story.segments, writers = story.writers)
            }.stateIn(viewModelScope, WhileUiSubscribed, SavedStoryUiState())
}
