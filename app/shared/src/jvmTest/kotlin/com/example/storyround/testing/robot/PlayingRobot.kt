package com.example.storyround.testing.robot

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import com.example.storyround.testing.robot.component.BannerRobot
import com.example.storyround.testing.robot.component.StoryTextRobot
import com.example.storyround.testing.robot.component.WritingFieldRobot

class PlayingRobot(
    ui: ComposeUiTest,
) : ScreenRobot(ui) {
    private val writingField = WritingFieldRobot(ui)
    private val storyText = StoryTextRobot(ui)
    private val banner = BannerRobot(ui)

    override val screenMarker = hasContentDescription(TIME_LEFT)

    fun enterContribution(text: String) = onScreen { writingField.enterText(text) }

    fun tapIdea() = onScreen { writingField.tapIdea() }

    fun tapSubmit() = onScreen { writingField.tapSend(SUBMIT) }

    fun pressKeyboardSend() = onScreen { writingField.pressKeyboardSend() }

    fun assertRound(
        round: Int,
        of: Int,
    ) = onScreen { assertTextShown("ROUND $round OF $of") }

    fun assertTurnNumber(
        turn: Int,
        of: Int,
    ) = onScreen { assertTextShown("Turn $turn of $of") }

    fun assertYourTurn() =
        onScreen {
            assertTextShown("YOUR TURN")
            writingField.assertShown()
        }

    fun assertTurnOf(name: String) =
        onScreen {
            assertTextShown("${name.uppercase()}’S TURN")
            banner.assertWaitingShown("$name is writing…")
            writingField.assertNotShown()
            assertTextNotShown(SUBMIT)
        }

    fun assertTurnSummary(summary: String) = onScreen { assertTextShown(summary) }

    fun assertCountdown(secondsLeft: String) = onScreen { assertTextShown(secondsLeft) }

    fun assertCountdownBarShown() = onScreen { await(hasContentDescription(TIME_LEFT) and hasProgress) }

    fun assertOneWordTag() = onScreen { assertTextShown("ONE WORD") }

    fun assertAskedForOneWord() =
        onScreen {
            writingField.assertLabelled("Add one word…")
            writingField.assertNoIdea()
        }

    fun assertPickingAWord(name: String) = onScreen { banner.assertWaitingShown("$name is picking a word…") }

    fun assertOneWordOnly() = onScreen { writingField.assertError(ONE_WORD_ONLY) }

    fun assertCounter(counter: String) = onScreen { writingField.assertCounter(counter) }

    fun assertSubmitEnabled(enabled: Boolean) = onScreen { writingField.assertSendEnabled(SUBMIT, enabled) }

    fun assertStoryShown(text: String) = onScreen { storyText.assertStoryShown(text) }

    fun assertNewestTag(tag: String) = onScreen { storyText.assertNewestTag(tag) }

    fun assertTimeUpToast(nextWriter: String) = onScreen { assertTextShown("Time’s up! Over to $nextWriter.") }

    fun assertReconnectingShown() = onScreen { banner.assertReconnectingShown() }

    private companion object {
        const val TIME_LEFT = "Time left"
        const val SUBMIT = "SUBMIT"
        const val ONE_WORD_ONLY = "One word only, no spaces"
        val hasProgress = SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)
    }
}
