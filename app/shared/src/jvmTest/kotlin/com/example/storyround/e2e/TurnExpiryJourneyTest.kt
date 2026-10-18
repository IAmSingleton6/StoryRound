package com.example.storyround.e2e

import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.FullStackTest
import com.example.storyround.testing.scenario.appHostingGame
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class TurnExpiryJourneyTest : FullStackTest() {
    @Test
    fun `should count down from the full turn when the app's turn starts`() =
        fullStackTest {
            GIVEN("Alex started a game in which Sam writes the opening") {
                appHostingGame(host = "Alex", guests = listOf("Sam"), opener = "Sam")
            }

            WHEN("Sam submits the prompt, starting Alex's 15-second turn") {
                player("Sam").submitPrompt("A door opened.")
            }

            THEN("Alex's countdown starts at 15.0 seconds") {
                playingRobot.assertYourTurn()
                playingRobot.assertCountdown("15.0")
            }
        }

    @Test
    fun `should say time is up and pass the story on when the app's turn runs out`() =
        fullStackTest {
            GIVEN("it is Alex's 15-second turn in a game with Sam") {
                appHostingGame(host = "Alex", guests = listOf("Sam"), opener = "Sam")
                player("Sam").submitPrompt("A door opened.")
                playingRobot.assertYourTurn()
            }

            WHEN("15 seconds pass on the server's clock") {
                clock.advanceBy(15.seconds)
            }

            THEN("Alex is told time is up and the story passes to Sam") {
                playingRobot.assertTimeUpToast("Sam")
                playingRobot.assertTurnOf("Sam")
            }
        }
}
