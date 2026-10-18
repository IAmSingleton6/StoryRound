package com.example.storyround.testing.robot

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.hasText
import com.example.storyround.testing.robot.component.BannerRobot
import kotlin.test.assertEquals

class HistoryRobot(
    ui: ComposeUiTest,
) : ScreenRobot(ui) {
    private val banner = BannerRobot(ui)

    override val screenMarker = hasText("Story history")

    fun tapStory(prompt: String) = onScreen { tap(prompt) }

    fun tapBack() = onScreen { tapDescribed("Back") }

    fun tapEmptyStateBack() = onScreen { tap("BACK") }

    fun assertStoriesShown(vararg prompts: String) = onScreen { prompts.forEach { assertTextShown(it) } }

    fun assertStoriesInOrder(vararg prompts: String) =
        onScreen {
            val tops = prompts.map { prompt -> await(hasText(prompt)).fetchSemanticsNode().boundsInRoot.top }
            assertEquals(tops.sorted(), tops, "Cards for ${prompts.toList()} should run top to bottom")
        }

    fun assertStorySummary(summary: String) = onScreen { assertTextShown(summary) }

    fun assertEmpty() =
        onScreen {
            assertTextShown("No completed stories yet.")
            assertTextShown("Finish a game and the story is saved here, on this device.")
        }

    fun assertErrorShown(message: String) = onScreen { banner.assertErrorShown(message) }
}
