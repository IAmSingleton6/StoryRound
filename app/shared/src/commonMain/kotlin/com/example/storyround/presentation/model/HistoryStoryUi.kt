package com.example.storyround.presentation.model

internal data class HistoryStoryUi(
    val id: String,
    val opening: String,
    val players: List<PlayerUi>,
    val contributionCount: Int,
    val writers: List<StoryWriterUi>,
)

internal fun StoryUi.toHistoryStory(): HistoryStoryUi {
    val segments = storySegments(prompt, promptAuthor, contributions)
    return HistoryStoryUi(
        id = id,
        opening = prompt,
        players = players,
        contributionCount = contributions.size,
        writers = storyWriters(players, segments).filter { it.pieces > 0 },
    )
}
