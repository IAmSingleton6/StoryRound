package com.example.storyround.e2e

import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.FullStackTest
import com.example.storyround.testing.scenario.appHostingGame
import kotlin.test.Test

class TextAgreementJourneyTest : FullStackTest() {
    @Test
    fun `should show the server's tidied text when a contribution has extra spacing`() =
        fullStackTest {
            GIVEN("Alex is writing the only turn of a solo game, with extra spaces and line breaks") {
                appHostingGame(host = "Alex", rounds = 1)
                openingPromptRobot.enterPrompt("A door opened.")
                openingPromptRobot.tapStartStory()
                playingRobot.enterContribution("  A  horse\n\nwalked in.  ")
            }

            WHEN("Alex submits the turn") {
                playingRobot.tapSubmit()
            }

            THEN("the finished story shows the text as the server tidied it") {
                finishedRobot.assertStoryShown("A door opened. A horse walked in.")
            }
        }
}
