package com.example.storyround.presentation.mapper

import com.example.storyround.presentation.model.ContributionUi
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.StorySegmentUi
import com.example.storyround.presentation.model.StoryWriterUi

internal class StorySegmentMapperImpl : StorySegmentMapper {
    override fun segments(
        prompt: String,
        promptAuthor: PlayerUi?,
        contributions: List<ContributionUi>,
    ): List<StorySegmentUi> =
        buildList {
            val openingLine = prompt.trim()
            if (openingLine.isNotEmpty()) add(StorySegmentUi(openingLine, promptAuthor))
            contributions.forEach { contribution ->
                val text = contribution.text.trim()
                if (text.isNotEmpty()) add(StorySegmentUi(text, contribution.player))
            }
        }

    override fun writers(
        players: List<PlayerUi>,
        segments: List<StorySegmentUi>,
    ): List<StoryWriterUi> = players.map { player -> StoryWriterUi(player, segments.count { it.author?.id == player.id }) }
}
