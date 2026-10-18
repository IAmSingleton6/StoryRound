package com.example.storyround.testing.robot

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.hasText
import com.example.storyround.testing.robot.component.StoryTextRobot
import com.example.storyround.testing.robot.component.WriterLegendRobot

class SavedStoryRobot(
    ui: ComposeUiTest,
) : ScreenRobot(ui) {
    private val storyText = StoryTextRobot(ui)
    private val legend = WriterLegendRobot(ui)

    override val screenMarker = hasText("SAVED STORY")

    fun tapBack() = onScreen { tapDescribed("Back") }

    fun tapBackToHistory() = onScreen { tap("Back to history") }

    fun assertStoryShown(text: String) = onScreen { storyText.assertStoryShown(text) }

    fun assertWriterShown(
        name: String,
        pieces: Int,
    ) = onScreen { legend.assertWriterShown(name, pieces) }
}
