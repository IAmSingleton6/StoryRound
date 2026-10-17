package com.example.storyround.presentation.finished

import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.ComponentTest
import com.example.storyround.testing.fixtures.finishedStory
import com.example.storyround.testing.fixtures.sam
import kotlin.test.Test

internal class FinishedScreenTest : ComponentTest<FinishedUiState, FinishedAction>({ state, onAction -> FinishedScreen(state, onAction) }) {
    @Test
    fun `should summarise the finished story when it is shown`() =
        componentTest {
            GIVEN("a story of one prompt and one contribution by two writers has finished") {
                useState(finishedStory())
            }

            WHEN("the finished screen is shown") {
                show()
            }

            THEN("it summarises the story and shows all of it") {
                finishedRobot.assertSummary("1 contribution · 2 writers · saved to your history")
                finishedRobot.assertStoryShown("A door opened. A horse arrived.")
            }
        }

    @Test
    fun `should mark the viewer's own chip when listing the writers`() =
        componentTest {
            GIVEN("Alex and Sam wrote one piece each and Alex is viewing") {
                useState(finishedStory())
            }

            WHEN("the finished screen is shown") {
                show()
            }

            THEN("Alex's chip says you and both show one piece") {
                finishedRobot.assertWriterShown("Alex (you)", pieces = 1)
                finishedRobot.assertWriterShown("Sam", pieces = 1)
            }
        }

    @Test
    fun `should offer no shortcut to view the story when it is already on screen`() =
        componentTest {
            GIVEN("a story has finished") {
                useState(finishedStory())
            }

            WHEN("the finished screen is shown") {
                show()
            }

            THEN("there is no view story or history shortcut") {
                finishedRobot.assertNoStoryShortcuts()
            }
        }

    @Test
    fun `should emit play again when the host presses play again`() =
        componentTest {
            GIVEN("Alex, the host, is viewing a finished story") {
                useState(finishedStory())
                show()
            }

            WHEN("Alex presses play again") {
                finishedRobot.tapPlayAgain()
            }

            THEN("the play again action is emitted") {
                actions.assertEmitted(FinishedAction.PlayAgain)
            }
        }

    @Test
    fun `should emit home when home is pressed`() =
        componentTest {
            GIVEN("a finished story is showing") {
                useState(finishedStory())
                show()
            }

            WHEN("the player presses home") {
                finishedRobot.tapHome()
            }

            THEN("the home action is emitted") {
                actions.assertEmitted(FinishedAction.Home)
            }
        }

    @Test
    fun `should tell a guest the host can play again when a guest views the finished story`() =
        componentTest {
            GIVEN("Sam, a guest, is viewing") {
                useState(finishedStory(viewerId = sam.id))
            }

            WHEN("the finished screen is shown") {
                show()
            }

            THEN("Sam is told Alex can start another round, with no replay button") {
                finishedRobot.assertHostCanReplay("Alex")
            }
        }

    @Test
    fun `should show the reconnecting banner when the connection has dropped`() =
        componentTest {
            GIVEN("the connection has dropped after the game finished") {
                useState(finishedStory(isReconnecting = true))
            }

            WHEN("the finished screen is shown") {
                show()
            }

            THEN("the reconnecting banner is shown") {
                finishedRobot.assertReconnectingShown()
            }
        }
}
