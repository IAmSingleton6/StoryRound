package com.example.storyround.presentation.model

internal data class RoomUi(
    val roomCode: String,
    val hostId: String,
    val players: List<PlayerUi>,
    val prompt: String,
    val contributions: List<ContributionUi>,
    val turn: TurnUi?,
    val promptAuthor: PlayerUi? = null,
    val maxRounds: Int,
)
