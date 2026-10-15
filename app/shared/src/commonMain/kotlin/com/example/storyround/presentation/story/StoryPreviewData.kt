package com.example.storyround.presentation.story

import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.RoomUi
import com.example.storyround.presentation.model.TurnUi
import com.example.storyround.ui.preview.PreviewPlayers
import com.example.storyround.ui.preview.PreviewStory

internal object StoryPreviewData {
    fun openingPrompt(isOpeningWriter: Boolean) =
        StoryUiState.OpeningPrompt(
            room = room(turn = null),
            openingPromptDraft = if (isOpeningWriter) PreviewStory.draft else "",
            openingPlayer = PreviewPlayers.alex,
            isOpeningWriter = isOpeningWriter,
        )

    fun playing(
        viewerId: String,
        activePlayer: PlayerUi = PreviewPlayers.alex,
        timeLeftMillis: Long = 9_400L,
        timeUpHandOver: PlayerUi? = null,
        isReconnecting: Boolean = false,
    ) = StoryUiState.Playing(
        room = room(turn = TurnUi(activePlayer, number = 5, deadlineEpochMillis = 0L)),
        activePlayer = activePlayer,
        playerId = viewerId,
        timeLeftMillis = timeLeftMillis,
        contributionDraft = if (viewerId == activePlayer.id) PreviewStory.draft else "",
        segments = PreviewStory.segments,
        round = 2,
        turn = 5,
        totalTurns = 8,
        turnDurationMillis = 15_000L,
        nextPlayer = nextPlayerAfter(activePlayer),
        nextTurnDurationMillis = 14_000L,
        timeUpHandOver = timeUpHandOver,
        isReconnecting = isReconnecting,
    )

    private fun nextPlayerAfter(player: PlayerUi): PlayerUi {
        val players = PreviewPlayers.all
        return players[(players.indexOf(player) + 1) % players.size]
    }

    private fun room(turn: TurnUi?) =
        RoomUi(
            roomCode = "7F2K",
            hostId = PreviewPlayers.alex.id,
            players = PreviewPlayers.all,
            maxPlayers = 10,
            prompt = PreviewStory.segments.first().text,
            contributions = emptyList(),
            turn = turn,
            maxRounds = 2,
            promptAuthor = PreviewPlayers.alex,
            segments = PreviewStory.segments,
            writers = PreviewStory.writers,
        )
}
