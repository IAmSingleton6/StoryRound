package com.example.storyround.presentation.model

internal data class StorySegmentUi(
    val text: String,
    val author: PlayerUi?,
)

internal fun storySegments(
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
