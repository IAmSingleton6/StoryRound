package com.example.storyround.presentation.model

internal data class RoomUi(
    val roomCode: String,
    val hostId: String,
    val players: List<PlayerUi>,
    val maxPlayers: Int,
    val prompt: String,
    val contributions: List<ContributionUi>,
    val turn: TurnUi?,
    val maxRounds: Int,
    val promptAuthor: PlayerUi? = null,
    val segments: List<StorySegmentUi> = emptyList(),
    val writers: List<StoryWriterUi> = emptyList(),
) {
    val host: PlayerUi?
        get() = players.firstOrNull { it.id == hostId }
}
