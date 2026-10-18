package com.example.storyround.e2e

import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.FullStackTest
import com.example.storyround.testing.scenario.appFinishedGame
import com.example.storyround.testing.scenario.appHostingGame
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

class ResumeJourneyTest : FullStackTest() {
    @Test
    fun `should resume the game where it left off when the app is reopened mid-game`() =
        fullStackTest {
            GIVEN("Alex closed the app during Alex's turn in a game with Sam") {
                appHostingGame(host = "Alex", guests = listOf("Sam"), opener = "Sam")
                player("Sam").submitPrompt("A door opened.")
                playingRobot.assertYourTurn()
                appRobot.close()
            }

            WHEN("Alex reopens the app") {
                appRobot.launch()
            }

            THEN("Alex is back on the story, still with the turn") {
                playingRobot.assertStoryShown("A door opened.")
                playingRobot.assertYourTurn()
            }
        }

    @Test
    fun `should land on home and forget the room when it was discarded while the app was closed`() =
        fullStackTest {
            GIVEN("Alex went home from a finished solo game, closed the app, and the room sat empty for ten minutes") {
                appFinishedGame(host = "Alex")
                clock.awaitTimerScheduledBy { finishedRobot.tapHome() }
                appRobot.close()
                clock.advanceBy(10.minutes)
            }

            WHEN("Alex reopens the app") {
                appRobot.launch()
            }

            THEN("Alex is on Home and the old session is forgotten") {
                homeRobot.assertShown()
                storageRobot.assertNoSessionSaved()
            }
        }
}
