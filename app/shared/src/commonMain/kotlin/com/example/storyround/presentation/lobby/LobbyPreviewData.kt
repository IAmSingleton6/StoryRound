package com.example.storyround.presentation.lobby

import com.example.storyround.ui.preview.PreviewPlayers

internal object LobbyPreviewData {
    fun host() =
        LobbyUiState(
            roomCode = "7F2K",
            players = PreviewPlayers.all,
            maxPlayers = 10,
            hostId = PreviewPlayers.alex.id,
            currentPlayerId = PreviewPlayers.alex.id,
            openingPlayerId = null,
            maxRounds = 2,
            canDecreaseRounds = true,
            canIncreaseRounds = true,
            totalTurns = 2 * PreviewPlayers.all.size,
        )

    fun guest() = host().copy(currentPlayerId = PreviewPlayers.sam.id)
}
