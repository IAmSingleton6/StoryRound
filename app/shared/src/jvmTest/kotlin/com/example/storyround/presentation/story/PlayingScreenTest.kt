package com.example.storyround.presentation.story

import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.ComponentTest
import com.example.storyround.testing.fixtures.alex
import com.example.storyround.testing.fixtures.alexWritingTurnThree
import com.example.storyround.testing.fixtures.sam
import kotlin.test.Test

internal class PlayingScreenTest : ComponentTest<StoryUiState, StoryAction>({ state, onAction -> StoryScreen(state, onAction) }) {
    @Test
    fun `should show the round and turn when a turn is in progress`() =
        componentTest {
            GIVEN("it is turn 3 of 4, in round 2 of 2") {
                useState(alexWritingTurnThree(viewerId = alex.id, timeLeftMillis = 7_300L))
            }

            WHEN("the story screen is shown") {
                show()
            }

            THEN("the round and turn are shown") {
                playingRobot.assertRound(2, of = 2)
                playingRobot.assertTurnNumber(3, of = 4)
            }
        }

    @Test
    fun `should show the viewer's turn with its countdown when it is their turn`() =
        componentTest {
            GIVEN("it is Alex's turn with 7.3 seconds left") {
                useState(alexWritingTurnThree(viewerId = alex.id, timeLeftMillis = 7_300L))
            }

            WHEN("Alex sees the story screen") {
                show()
            }

            THEN("it says it is Alex's turn and counts down from 7.3 seconds") {
                playingRobot.assertYourTurn()
                playingRobot.assertCountdown("7.3")
                playingRobot.assertCountdownBarShown()
            }
        }

    @Test
    fun `should name the next writer and their time when another turn follows`() =
        componentTest {
            GIVEN("Alex is writing a 13-second turn and Sam's 12-second turn is next") {
                useState(alexWritingTurnThree(viewerId = alex.id, timeLeftMillis = 7_300L))
            }

            WHEN("Alex sees the story screen") {
                show()
            }

            THEN("the turn card names Sam and the next turn's length") {
                playingRobot.assertTurnSummary("13s turn · next: Sam, 12s")
            }
        }

    @Test
    fun `should say it is the final turn when nobody writes next`() =
        componentTest {
            GIVEN("Alex is writing the last turn") {
                useState(alexWritingTurnThree(viewerId = alex.id, timeLeftMillis = 3_000L, nextPlayer = null))
            }

            WHEN("Alex sees the story screen") {
                show()
            }

            THEN("the turn card says it is the final turn") {
                playingRobot.assertTurnSummary("13s turn · final turn")
            }
        }

    @Test
    fun `should disable submit when the draft is blank`() =
        componentTest {
            GIVEN("it is Alex's turn and Alex has not written anything") {
                useState(alexWritingTurnThree(viewerId = alex.id, timeLeftMillis = 3_000L))
            }

            WHEN("Alex sees the story screen") {
                show()
            }

            THEN("submit is disabled") {
                playingRobot.assertSubmitEnabled(false)
            }
        }

    @Test
    fun `should emit submit contribution when submit is pressed with a draft`() =
        componentTest {
            GIVEN("it is Alex's turn and Alex has written \"A horse arrived.\"") {
                useState(alexWritingTurnThree(viewerId = alex.id, timeLeftMillis = 7_300L, draft = "A horse arrived."))
                show()
            }

            WHEN("Alex presses submit") {
                playingRobot.tapSubmit()
            }

            THEN("the contribution is submitted") {
                actions.assertEmitted(StoryAction.SubmitContribution)
            }
        }

    @Test
    fun `should emit the typed text when the writer types`() =
        componentTest {
            GIVEN("it is Alex's turn with an empty writing field") {
                useState(alexWritingTurnThree(viewerId = alex.id, timeLeftMillis = 7_300L))
                show()
            }

            WHEN("Alex types \"A horse arrived.\"") {
                playingRobot.enterContribution("A horse arrived.")
            }

            THEN("the typed text is emitted") {
                actions.assertEmitted(StoryAction.ContributionChanged("A horse arrived."))
            }
        }

    @Test
    fun `should emit a random idea request when idea is pressed`() =
        componentTest {
            GIVEN("it is Alex's turn") {
                useState(alexWritingTurnThree(viewerId = alex.id, timeLeftMillis = 7_300L))
                show()
            }

            WHEN("Alex presses idea") {
                playingRobot.tapIdea()
            }

            THEN("a random idea is requested") {
                actions.assertEmitted(StoryAction.RandomContribution)
            }
        }

    @Test
    fun `should wait for the writer when it is someone else's turn`() =
        componentTest {
            GIVEN("it is Alex's turn and Sam is watching") {
                useState(alexWritingTurnThree(viewerId = sam.id, timeLeftMillis = 4_200L))
            }

            WHEN("Sam sees the story screen") {
                show()
            }

            THEN("Sam sees Alex is writing and has nothing to submit") {
                playingRobot.assertTurnOf("Alex")
            }
        }

    @Test
    fun `should call the viewer you when they write next`() =
        componentTest {
            GIVEN("it is Alex's turn and Sam writes next") {
                useState(alexWritingTurnThree(viewerId = sam.id, timeLeftMillis = 4_200L))
            }

            WHEN("Sam sees the story screen") {
                show()
            }

            THEN("the turn card says Sam is next as you") {
                playingRobot.assertTurnSummary("13s turn · next: you, 12s")
            }
        }

    @Test
    fun `should show the story so far and tag its newest piece when someone else is writing`() =
        componentTest {
            GIVEN("Alex's prompt is the newest piece of the story") {
                useState(alexWritingTurnThree(viewerId = sam.id, timeLeftMillis = 4_200L))
            }

            WHEN("Sam sees the story screen") {
                show()
            }

            THEN("the story is shown with its newest piece tagged with Alex's name") {
                playingRobot.assertStoryShown("A door opened.")
                playingRobot.assertNewestTag("ALEX")
            }
        }

    @Test
    fun `given the viewer wrote the newest piece when the story screen is shown then it is tagged YOU`() =
        componentTest {
            GIVEN("Alex's prompt is the newest piece and Alex is watching") {
                useState(alexWritingTurnThree(viewerId = alex.id, timeLeftMillis = 4_200L))
            }

            WHEN("Alex sees the story screen") {
                show()
            }

            THEN("the newest piece is tagged YOU") {
                playingRobot.assertNewestTag("YOU")
            }
        }

    @Test
    fun `given a turn expired and passed to the viewer when the story screen is shown then the toast says it is over to them`() =
        componentTest {
            GIVEN("Sam's turn just expired and it passed to Alex, who is watching") {
                useState(alexWritingTurnThree(viewerId = alex.id, timeLeftMillis = 12_000L, timeUpHandOver = alex))
            }

            WHEN("Alex sees the story screen") {
                show()
            }

            THEN("a toast says time is up and it is over to them") {
                playingRobot.assertTimeUpToast("you")
            }
        }

    @Test
    fun `should name the next writer in a toast when a turn has expired`() =
        componentTest {
            GIVEN("Sam's turn just expired and it passed to Alex") {
                useState(alexWritingTurnThree(viewerId = sam.id, timeLeftMillis = 12_000L, timeUpHandOver = alex))
            }

            WHEN("Sam sees the story screen") {
                show()
            }

            THEN("a toast says time is up and names Alex") {
                playingRobot.assertTimeUpToast("Alex")
            }
        }

    @Test
    fun `should show the reconnecting banner when the connection drops during play`() =
        componentTest {
            GIVEN("the connection has dropped during Alex's turn") {
                useState(alexWritingTurnThree(viewerId = sam.id, timeLeftMillis = 4_200L, isReconnecting = true))
            }

            WHEN("Sam sees the story screen") {
                show()
            }

            THEN("the reconnecting banner is shown") {
                playingRobot.assertReconnectingShown()
            }
        }

    @Test
    fun `given a one word game when it is the viewer's turn then the field asks for one word without ideas`() =
        componentTest {
            GIVEN("it is Alex's turn in a one word game") {
                useState(alexWritingTurnThree(viewerId = alex.id, timeLeftMillis = 7_300L, oneWordPerTurn = true))
            }

            WHEN("Alex sees the story screen") {
                show()
            }

            THEN("the turn is tagged one word and the field asks for one word of up to 30 characters") {
                playingRobot.assertOneWordTag()
                playingRobot.assertAskedForOneWord()
                playingRobot.assertCounter("0/30")
            }
        }

    @Test
    fun `given a one word game when the draft has two words then the rule is explained and submit is disabled`() =
        componentTest {
            GIVEN("Alex has typed two words in a one word game") {
                useState(
                    alexWritingTurnThree(
                        viewerId = alex.id,
                        timeLeftMillis = 7_300L,
                        draft = "a horse",
                        oneWordPerTurn = true,
                        isDraftValid = false,
                    ),
                )
            }

            WHEN("Alex sees the story screen") {
                show()
            }

            THEN("Alex is told it must be one word and cannot submit") {
                playingRobot.assertOneWordOnly()
                playingRobot.assertSubmitEnabled(false)
            }
        }

    @Test
    fun `given a one word game with a word typed when the keyboard send key is pressed then the word is submitted`() =
        componentTest {
            GIVEN("Alex has typed one word in a one word game") {
                useState(alexWritingTurnThree(viewerId = alex.id, timeLeftMillis = 7_300L, draft = "horse", oneWordPerTurn = true))
                show()
            }

            WHEN("Alex presses the keyboard's send key") {
                playingRobot.pressKeyboardSend()
            }

            THEN("the word is submitted") {
                actions.assertEmitted(StoryAction.SubmitContribution)
            }
        }

    @Test
    fun `given a one word game when it is someone else's turn then they are shown picking a word`() =
        componentTest {
            GIVEN("Sam is watching Alex's turn in a one word game") {
                useState(alexWritingTurnThree(viewerId = sam.id, timeLeftMillis = 4_200L, oneWordPerTurn = true))
            }

            WHEN("Sam sees the story screen") {
                show()
            }

            THEN("Alex is shown picking a word") {
                playingRobot.assertPickingAWord("Alex")
                playingRobot.assertOneWordTag()
            }
        }
}
