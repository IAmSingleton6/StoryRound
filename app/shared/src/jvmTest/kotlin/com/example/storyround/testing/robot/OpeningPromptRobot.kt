package com.example.storyround.testing.robot

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.hasText
import com.example.storyround.testing.robot.component.BannerRobot
import com.example.storyround.testing.robot.component.PlayerStripRobot
import com.example.storyround.testing.robot.component.WritingFieldRobot

class OpeningPromptRobot(
    ui: ComposeUiTest,
) : ScreenRobot(ui) {
    private val writingField = WritingFieldRobot(ui)
    private val playerStrip = PlayerStripRobot(ui)
    private val banner = BannerRobot(ui)

    override val screenMarker = hasText("START THE STORY")

    fun enterPrompt(text: String) = onScreen { writingField.enterText(text) }

    fun tapIdea() = onScreen { writingField.tapIdea() }

    fun tapStartStory() = onScreen { writingField.tapSend(START_STORY) }

    fun assertNoTimer() =
        onScreen {
            assertTextShown("NO TIMER")
            assertTextShown("No timer yet")
        }

    fun assertAskedForPrompt() =
        onScreen {
            assertTextShown("Write the opening line or premise. Take your time.")
            writingField.assertShown()
        }

    fun assertAskedForFirstWord() =
        onScreen {
            assertTextShown("Write the first word. Take your time.")
            assertTextShown("The first word becomes the first coloured piece of the story.")
            writingField.assertLabelled("Start with one word…")
            writingField.assertNoIdea()
        }

    fun assertChoosingFirstWord(opener: String) = onScreen { assertTextShown("$opener is choosing the first word…") }

    fun assertOneWordOnly() = onScreen { writingField.assertError("One word only, no spaces") }

    fun assertCounter(counter: String) = onScreen { writingField.assertCounter(counter) }

    fun assertDraft(text: String) = onScreen { assertTextShown(text) }

    fun assertStartEnabled(enabled: Boolean) = onScreen { writingField.assertSendEnabled(START_STORY, enabled) }

    fun assertWaitingFor(opener: String) =
        onScreen {
            assertTextShown("$opener is starting the story…")
            banner.assertWaitingShown("Waiting for $opener…")
            writingField.assertNotShown()
            assertTextNotShown(START_STORY)
        }

    fun assertStartingTheStory(opener: String) = onScreen { assertTextShown("$opener is starting the story…") }

    fun assertOpenerHighlighted(opener: String) = onScreen { playerStrip.assertWriting(opener) }

    fun assertReconnectingShown() = onScreen { banner.assertReconnectingShown() }

    private companion object {
        const val START_STORY = "START STORY"
    }
}
