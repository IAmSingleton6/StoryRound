package com.example.storyround.e2e

import com.example.storyround.testing.AND
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.FullStackTest
import com.example.storyround.testing.scenario.appHostingGame
import kotlin.test.Test

class SoloGameJourneyTest : FullStackTest() {
    @Test
    fun `should finish and save the story when a solo player writes the only turn`() =
        fullStackTest {
            GIVEN("Alex opened a solo one-round game with \"A door opened.\" and has written the only turn") {
                appHostingGame(host = "Alex", rounds = 1)
                openingPromptRobot.enterPrompt("A door opened.")
                openingPromptRobot.tapStartStory()
                playingRobot.enterContribution("The end.")
            }

            WHEN("Alex submits the turn") {
                playingRobot.tapSubmit()
            }

            THEN("Alex sees the finished story") {
                finishedRobot.assertStoryShown("A door opened. The end.")
            }

            AND("the story is saved on the device") {
                storageRobot.assertHistory("A door opened.")
            }
        }

    @Test
    fun `should show the saved story in history when the player goes home after finishing`() =
        fullStackTest {
            GIVEN("Alex finished a solo game and went home") {
                appHostingGame(host = "Alex", rounds = 1)
                openingPromptRobot.enterPrompt("A door opened.")
                openingPromptRobot.tapStartStory()
                playingRobot.enterContribution("The end.")
                playingRobot.tapSubmit()
                finishedRobot.tapHome()
            }

            WHEN("Alex opens story history") {
                homeRobot.tapHistory()
            }

            THEN("the finished story is listed") {
                historyRobot.assertStoriesShown("A door opened.")
            }
        }
}
