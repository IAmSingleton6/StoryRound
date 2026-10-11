package com.example.storyround.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.presentation.mapper.SavedStoryUiMapper
import com.example.storyround.presentation.model.storySegments
import com.example.storyround.presentation.model.storyWriters
import kotlinx.coroutines.flow.SharingStarted
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
                val story = state.selectedStory?.let(savedStoryUiMapper::map)
                val segments = story?.let { storySegments(it.prompt, it.promptAuthor, it.contributions) }.orEmpty()
                SavedStoryUiState(
                    story = story,
                    segments = segments,
                    writers = story?.let { storyWriters(it.players, segments) }.orEmpty(),
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SavedStoryUiState())
}
