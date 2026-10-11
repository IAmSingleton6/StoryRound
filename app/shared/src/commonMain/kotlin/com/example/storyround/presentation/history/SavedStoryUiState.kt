package com.example.storyround.presentation.history

import com.example.storyround.presentation.model.StorySegmentUi
import com.example.storyround.presentation.model.StoryUi
import com.example.storyround.presentation.model.StoryWriterUi

internal data class SavedStoryUiState(
    val story: StoryUi? = null,
    val segments: List<StorySegmentUi> = emptyList(),
    val writers: List<StoryWriterUi> = emptyList(),
)
