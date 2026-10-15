package com.example.storyround.ui.preview

import com.example.storyround.presentation.model.StorySegmentUi
import com.example.storyround.presentation.model.StoryWriterUi
import com.example.storyround.ui.preview.PreviewPlayers.alex
import com.example.storyround.ui.preview.PreviewPlayers.jordan
import com.example.storyround.ui.preview.PreviewPlayers.priya
import com.example.storyround.ui.preview.PreviewPlayers.sam

internal object PreviewStory {
    val segments =
        listOf(
            StorySegmentUi("The lighthouse keeper found a door at the bottom of the stairs that had not been there yesterday.", alex),
            StorySegmentUi("Behind it, a spiral of wet stone led down beneath the sea.", sam),
            StorySegmentUi("At the very bottom sat a small octopus wearing a tiny brass key around its neck.", priya),
            StorySegmentUi("\"You're late,\" it said, tapping a pocket watch with three of its arms.", jordan),
        )

    val writers =
        listOf(
            StoryWriterUi(alex, 3),
            StoryWriterUi(sam, 2),
            StoryWriterUi(priya, 2),
            StoryWriterUi(jordan, 1),
        )

    val draft = "The keeper checked their own watch. It had stopped at exactly midnight."
}
