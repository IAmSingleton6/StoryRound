package com.example.storyround.presentation.mapper

import com.example.storyround.presentation.model.ContributionUi
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.StorySegmentUi
import com.example.storyround.presentation.model.StoryWriterUi

internal interface StorySegmentMapper {
    fun segments(
        prompt: String,
        promptAuthor: PlayerUi?,
        contributions: List<ContributionUi>,
    ): List<StorySegmentUi>

    fun writers(
        players: List<PlayerUi>,
        segments: List<StorySegmentUi>,
    ): List<StoryWriterUi>
}
