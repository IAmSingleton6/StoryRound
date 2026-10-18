package com.example.storyround.e2e

import com.example.storyround.testing.AND
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.FullStackTest
import com.example.storyround.testing.scenario.appHostingGame
import kotlin.test.Test

class OneWordJourneyTest : FullStackTest() {
    @Test
    fun `given a one word game hosted from the app when the host types two words then they are told it must be one word`() =
        fullStackTest {
            GIVEN("Alex started a one word game from the app and is asked for the first word") {
                appHostingGame(host = "Alex", guests = listOf("Sam"), oneWordPerTurn = true)
            }

            WHEN("Alex types two words") {
                openingPromptRobot.enterPrompt("A door")
            }

            THEN("Alex is told it must be one word and cannot start the story") {
                openingPromptRobot.assertOneWordOnly()
                openingPromptRobot.assertStartEnabled(false)
            }
        }

    @Test
    fun `given a one word game when the host adds a word on their turn then the next player is shown picking a word`() =
        fullStackTest {
            GIVEN("Alex opened a one word game with Sam with the word Once and it is Alex's turn") {
                appHostingGame(host = "Alex", guests = listOf("Sam"), oneWordPerTurn = true)
                openingPromptRobot.enterPrompt("Once")
                openingPromptRobot.tapStartStory()
                playingRobot.assertOneWordTag()
                playingRobot.assertAskedForOneWord()
                playingRobot.enterContribution("upon")
            }

            WHEN("Alex submits the word") {
                playingRobot.tapSubmit()
            }

            THEN("the story grows by one word and Sam is shown picking the next") {
                playingRobot.assertStoryShown("Once upon")
                playingRobot.assertPickingAWord("Sam")
            }
        }

    @Test
    fun `given a one word game when the last player adds their word then everyone sees the story built word by word`() =
        fullStackTest {
            GIVEN("Alex and Sam have written Once upon in a one round one word game") {
                appHostingGame(host = "Alex", guests = listOf("Sam"), oneWordPerTurn = true)
                openingPromptRobot.enterPrompt("Once")
                openingPromptRobot.tapStartStory()
                playingRobot.enterContribution("upon")
                playingRobot.tapSubmit()
            }

            WHEN("Sam adds the final word") {
                player("Sam").submitContribution("a")
            }

            THEN("Alex sees the finished story") {
                finishedRobot.assertStoryShown("Once upon a")
            }

            AND("Sam received the same story") {
                player("Sam").assertStoryFinished("Once", "upon", "a")
            }
        }
}
