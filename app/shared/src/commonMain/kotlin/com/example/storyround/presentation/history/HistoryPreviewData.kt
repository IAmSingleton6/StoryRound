package com.example.storyround.presentation.history

import com.example.storyround.presentation.model.ContributionUi
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.StoryUi
import com.example.storyround.presentation.model.StoryWriterUi
import com.example.storyround.ui.preview.PreviewPlayers.alex
import com.example.storyround.ui.preview.PreviewPlayers.jordan
import com.example.storyround.ui.preview.PreviewPlayers.priya
import com.example.storyround.ui.preview.PreviewPlayers.sam
import com.example.storyround.ui.preview.PreviewStory

internal object HistoryPreviewData {
    val stories =
        listOf(
            story(id = "story-1", prompt = PreviewStory.segments.first().text, players = listOf(alex, sam, priya, jordan)),
            story(id = "story-2", prompt = "A dragon applied for a job at the local bakery.", players = listOf(sam, alex)),
            story(id = "story-3", prompt = "Every clock in town ran backwards on Tuesday.", players = listOf(priya, jordan, sam)),
        )

    fun savedStory() = SavedStoryUiState(segments = PreviewStory.segments, writers = PreviewStory.writers)

    private fun story(
        id: String,
        prompt: String,
        players: List<PlayerUi>,
    ) = StoryUi(
        id = id,
        prompt = prompt,
        players = players,
        contributions = List(players.size * 2) { order -> ContributionUi(players[order % players.size], "Piece $order.", order) },
        promptAuthor = players.first(),
        writers = players.mapIndexed { index, player -> StoryWriterUi(player, pieces = index + 1) },
    )
}
