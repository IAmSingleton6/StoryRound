package com.example.storyround.presentation.model

internal data class TurnUi(
    val player: PlayerUi,
    val number: Int,
    val deadlineEpochMillis: Long,
)
