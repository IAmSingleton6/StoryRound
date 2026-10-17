package com.example.storyround.presentation.story

import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.ComponentTest
import com.example.storyround.testing.fixtures.openingPrompt
import kotlin.test.Test

internal class OpeningPromptScreenTest : ComponentTest<StoryUiState, StoryAction>({ state, onAction -> StoryScreen(state, onAction) }) {
    @Test
    fun `should ask the opener for the prompt without a timer when the opener is writing`() =
        componentTest {
            GIVEN("Alex is the opening writer") {
                useState(openingPrompt(isOpeningWriter = true))
            }

            WHEN("Alex sees the story screen") {
                show()
            }

            THEN("Alex is asked for the opening line, with no timer") {
                openingPromptRobot.assertAskedForPrompt()
                openingPromptRobot.assertNoTimer()
            }
        }

    @Test
    fun `should disable start story when the draft is blank`() =
        componentTest {
            GIVEN("the opener has not written anything") {
                useState(openingPrompt(isOpeningWriter = true, draft = ""))
            }

            WHEN("the story screen is shown") {
                show()
            }

            THEN("the counter reads zero and start story is disabled") {
                openingPromptRobot.assertCounter("0/280")
                openingPromptRobot.assertStartEnabled(false)
            }
        }

    @Test
    fun `should count the draft's characters when the opener has written something`() =
        componentTest {
            GIVEN("the opener has written \"A door opened.\"") {
                useState(openingPrompt(isOpeningWriter = true, draft = "A door opened."))
            }

            WHEN("the story screen is shown") {
                show()
            }

            THEN("the counter reads 14 of 280 and start story is enabled") {
                openingPromptRobot.assertCounter("14/280")
                openingPromptRobot.assertStartEnabled(true)
            }
        }

    @Test
    fun `should show the count near the limit when the draft is long`() =
        componentTest {
            GIVEN("the opener has written 260 characters") {
                useState(openingPrompt(isOpeningWriter = true, draft = "a".repeat(260)))
            }

            WHEN("the story screen is shown") {
                show()
            }

            THEN("the counter reads 260 of 280") {
                openingPromptRobot.assertCounter("260/280")
            }
        }

    @Test
    fun `should emit submit opening prompt when start story is pressed with a draft`() =
        componentTest {
            GIVEN("the opener has written \"A door opened.\"") {
                useState(openingPrompt(isOpeningWriter = true, draft = "A door opened."))
                show()
            }

            WHEN("the opener presses start story") {
                openingPromptRobot.tapStartStory()
            }

            THEN("the prompt is submitted") {
                actions.assertEmitted(StoryAction.SubmitOpeningPrompt)
            }
        }

    @Test
    fun `should emit each change when the opener types`() =
        componentTest {
            GIVEN("the opener has an empty writing field") {
                useState(openingPrompt(isOpeningWriter = true))
                show()
            }

            WHEN("the opener types \"A door opened.\"") {
                openingPromptRobot.enterPrompt("A door opened.")
            }

            THEN("the typed text is emitted") {
                actions.assertEmitted(StoryAction.OpeningPromptChanged("A door opened."))
            }
        }

    @Test
    fun `should emit a random premise request when idea is pressed`() =
        componentTest {
            GIVEN("the opener has an empty writing field") {
                useState(openingPrompt(isOpeningWriter = true))
                show()
            }

            WHEN("the opener presses idea") {
                openingPromptRobot.tapIdea()
            }

            THEN("a random opening premise is requested") {
                actions.assertEmitted(StoryAction.RandomOpeningPrompt)
            }
        }

    @Test
    fun `should show a waiting pill instead of the writing field when someone else opens`() =
        componentTest {
            GIVEN("Alex is opening and Sam is watching") {
                useState(openingPrompt(isOpeningWriter = false))
            }

            WHEN("Sam sees the story screen") {
                show()
            }

            THEN("Sam waits for Alex with nothing to type into") {
                openingPromptRobot.assertWaitingFor("Alex")
            }
        }

    @Test
    fun `given the opening writer is unknown when another player sees the opening then it still reads as a sentence`() =
        componentTest {
            GIVEN("Sam is waiting for an opener who is not in the room list") {
                useState(openingPrompt(isOpeningWriter = false, openingPlayer = null))
            }

            WHEN("Sam sees the story screen") {
                show()
            }

            THEN("it says the opening writer is starting the story") {
                openingPromptRobot.assertStartingTheStory("The opening writer")
            }
        }

    @Test
    fun `should highlight the opener in the player strip when the prompt is being written`() =
        componentTest {
            GIVEN("Alex is opening") {
                useState(openingPrompt(isOpeningWriter = false))
            }

            WHEN("the story screen is shown") {
                show()
            }

            THEN("the strip announces Alex as writing now") {
                openingPromptRobot.assertOpenerHighlighted("Alex")
            }
        }

    @Test
    fun `should show the reconnecting banner when the connection has dropped`() =
        componentTest {
            GIVEN("the connection has dropped while waiting for the prompt") {
                useState(openingPrompt(isOpeningWriter = false, isReconnecting = true))
            }

            WHEN("the story screen is shown") {
                show()
            }

            THEN("the reconnecting banner is shown") {
                openingPromptRobot.assertReconnectingShown()
            }
        }

    @Test
    fun `given a one word game when the opener is writing then the first word is asked for without ideas`() =
        componentTest {
            GIVEN("Alex is the opening writer of a one word game") {
                useState(openingPrompt(isOpeningWriter = true, oneWordPerTurn = true))
            }

            WHEN("Alex sees the story screen") {
                show()
            }

            THEN("Alex is asked for the first word of up to 30 characters") {
                openingPromptRobot.assertAskedForFirstWord()
                openingPromptRobot.assertCounter("0/30")
            }
        }

    @Test
    fun `given a one word game when the opening draft has two words then the rule is explained and start is disabled`() =
        componentTest {
            GIVEN("Alex has typed two words to open a one word game") {
                useState(openingPrompt(isOpeningWriter = true, draft = "A door", oneWordPerTurn = true, isDraftValid = false))
            }

            WHEN("Alex sees the story screen") {
                show()
            }

            THEN("Alex is told it must be one word and cannot start") {
                openingPromptRobot.assertOneWordOnly()
                openingPromptRobot.assertStartEnabled(false)
            }
        }

    @Test
    fun `given a one word game when someone else opens then they are shown choosing the first word`() =
        componentTest {
            GIVEN("Sam is waiting for Alex to open a one word game") {
                useState(openingPrompt(isOpeningWriter = false, oneWordPerTurn = true))
            }

            WHEN("Sam sees the story screen") {
                show()
            }

            THEN("Alex is shown choosing the first word") {
                openingPromptRobot.assertChoosingFirstWord("Alex")
            }
        }
}
