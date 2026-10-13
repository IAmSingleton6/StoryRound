package com.example.storyround.presentation.model

internal data class StoryUi(
    val id: String,
    val prompt: String,
    val players: List<PlayerUi>,
    val contributions: List<ContributionUi>,
    val promptAuthor: PlayerUi? = null,
    val segments: List<StorySegmentUi> = emptyList(),
    val writers: List<StoryWriterUi> = emptyList(),
)
