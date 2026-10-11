package com.example.storyround.presentation.model

internal data class StoryWriterUi(
    val player: PlayerUi,
    val pieces: Int,
)

internal fun storyWriters(
    players: List<PlayerUi>,
    segments: List<StorySegmentUi>,
): List<StoryWriterUi> = players.map { player -> StoryWriterUi(player, segments.count { it.author?.id == player.id }) }
