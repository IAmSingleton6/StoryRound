package com.example.storyround.presentation.history

import com.example.storyround.presentation.model.StorySegmentUi
import com.example.storyround.presentation.model.StoryWriterUi

internal data class SavedStoryUiState(
    val segments: List<StorySegmentUi> = emptyList(),
    val writers: List<StoryWriterUi> = emptyList(),
)
