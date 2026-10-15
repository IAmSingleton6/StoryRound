package com.example.storyround.presentation.finished

import com.example.storyround.ui.preview.PreviewPlayers
import com.example.storyround.ui.preview.PreviewStory

internal object FinishedPreviewData {
    fun finished(viewerId: String) =
        FinishedUiState(
            segments = PreviewStory.segments,
            writers = PreviewStory.writers,
            contributionCount = PreviewStory.segments.size - 1,
            host = PreviewPlayers.alex,
            playerId = viewerId,
            canPlayAgain = viewerId == PreviewPlayers.alex.id,
        )
}
