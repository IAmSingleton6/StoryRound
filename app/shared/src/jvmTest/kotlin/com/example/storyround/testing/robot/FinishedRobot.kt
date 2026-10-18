package com.example.storyround.testing.robot

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.hasText
import com.example.storyround.testing.robot.component.BannerRobot
import com.example.storyround.testing.robot.component.StoryTextRobot
import com.example.storyround.testing.robot.component.WriterLegendRobot

class FinishedRobot(
    ui: ComposeUiTest,
) : ScreenRobot(ui) {
    private val storyText = StoryTextRobot(ui)
    private val legend = WriterLegendRobot(ui)
    private val banner = BannerRobot(ui)

    override val screenMarker = hasText("STORY COMPLETE")

    fun tapPlayAgain() = onScreen { tap(PLAY_AGAIN) }

    fun tapHome() = onScreen { tap("Home") }

    fun assertSummary(summary: String) = onScreen { assertTextShown(summary) }

    fun assertStoryShown(text: String) = onScreen { storyText.assertStoryShown(text) }

    fun assertWriterShown(
        name: String,
        pieces: Int,
    ) = onScreen { legend.assertWriterShown(name, pieces) }

    fun assertPlayAgainShown() = onScreen { assertControlEnabled(PLAY_AGAIN, enabled = true) }

    fun assertHostCanReplay(host: String) =
        onScreen {
            assertTextShown("$host can start another round from here.")
            assertTextNotShown(PLAY_AGAIN)
        }

    fun assertNoStoryShortcuts() =
        onScreen {
            assertTextNotShown("View story")
            assertTextNotShown("Story history")
        }

    fun assertReconnectingShown() = onScreen { banner.assertReconnectingShown() }

    private companion object {
        const val PLAY_AGAIN = "PLAY AGAIN"
    }
}
