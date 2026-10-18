package com.example.storyround.flow

import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.testing.AND
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.AppFlowTest
import com.example.storyround.testing.fixtures.ROOM_CODE
import com.example.storyround.testing.fixtures.alexPlayer
import com.example.storyround.testing.fixtures.playingRoom
import com.example.storyround.testing.fixtures.promptRoom
import com.example.storyround.testing.fixtures.samPlayer
import com.example.storyround.testing.fixtures.tokenOf
import com.example.storyround.testing.scenario.appResumedInto
import kotlin.test.Test

class StoryFlowTest : AppFlowTest() {
    @Test
    fun `should fill in a bundled opening premise when the opener asks for an idea`() =
        appFlowTest {
            GIVEN("Alex is being asked for the opening prompt and random picks choose the first option") {
                appResumedInto(promptRoom(opener = alexPlayer), alexPlayer)
            }

            WHEN("Alex presses idea") {
                openingPromptRobot.tapIdea()
            }

            THEN("the first bundled premise fills the writing field") {
                openingPromptRobot.assertDraft("The lighthouse keeper had not seen a ship in forty years, until this morning.")
            }
        }

    @Test
    fun `should send the prompt when the opener starts the story`() =
        appFlowTest {
            GIVEN("Alex has written an opening prompt") {
                appResumedInto(promptRoom(opener = alexPlayer), alexPlayer)
                openingPromptRobot.enterPrompt("A door opened.")
            }

            WHEN("Alex presses start story") {
                openingPromptRobot.tapStartStory()
            }

            THEN("the app sends Alex's prompt") {
                serverRobot.assertSent(ClientMessage.SubmitPrompt(ROOM_CODE, alexPlayer.id, "A door opened."))
            }
        }

    @Test
    fun `should send the contribution when the writer submits it`() =
        appFlowTest {
            GIVEN("it is Alex's turn and Alex has written a contribution") {
                appResumedInto(playingRoom(writer = alexPlayer, deadlineEpochMillis = clock.nowEpochMillis + 15_000), alexPlayer)
                playingRobot.enterContribution("A horse walked in.")
            }

            WHEN("Alex presses submit") {
                playingRobot.tapSubmit()
            }

            THEN("the app sends Alex's contribution") {
                serverRobot.assertSent(ClientMessage.SubmitContribution(ROOM_CODE, alexPlayer.id, "A horse walked in."))
            }
        }

    @Test
    fun `should count down from the server's deadline when a turn is in progress`() =
        appFlowTest {
            GIVEN("the app has launched with Alex's saved session and asked to reconnect") {
                storageRobot.saveSession(ROOM_CODE, alexPlayer.id, tokenOf(alexPlayer))
                appRobot.launch()
                serverRobot.awaitSent(ClientMessage.Reconnect(ROOM_CODE, alexPlayer.id, tokenOf(alexPlayer)))
            }

            WHEN("the server restores a turn of Alex's whose deadline is 12.4 seconds away on the device's clock") {
                serverRobot.resume(alexPlayer, playingRoom(writer = alexPlayer, deadlineEpochMillis = clock.nowEpochMillis + 12_400))
            }

            THEN("the countdown reads 12.4 seconds") {
                playingRobot.assertCountdown("12.4")
            }
        }

    @Test
    fun `should grow the story and pass the turn when another player's contribution arrives`() =
        appFlowTest {
            GIVEN("Sam is watching Alex write the first turn") {
                appResumedInto(playingRoom(writer = alexPlayer, deadlineEpochMillis = clock.nowEpochMillis + 15_000), samPlayer)
                playingRobot.assertTurnOf("Alex")
            }

            WHEN("the server adds Alex's contribution and starts Sam's turn") {
                val contribution = Contribution(alexPlayer.id, "A horse walked in.", turnNumber = 0)
                serverRobot.sendMessage(ServerMessage.ContributionAdded(contribution))
                serverRobot.startTurn(
                    playingRoom(
                        writer = samPlayer,
                        turnNumber = 1,
                        contributions = listOf(contribution),
                        deadlineEpochMillis = clock.nowEpochMillis + 14_000,
                    ),
                )
            }

            THEN("the story includes Alex's contribution") {
                playingRobot.assertStoryShown("A door opened. A horse walked in.")
            }

            AND("it is Sam's turn") {
                playingRobot.assertYourTurn()
            }
        }

    @Test
    fun `should say time is up and name the next writer when a turn expires`() =
        appFlowTest {
            GIVEN("Alex is writing the first turn of a game with Sam") {
                appResumedInto(playingRoom(writer = alexPlayer, deadlineEpochMillis = clock.nowEpochMillis + 15_000), alexPlayer)
                playingRobot.assertYourTurn()
            }

            WHEN("the server moves on to Sam's turn without Alex's contribution") {
                serverRobot.startTurn(playingRoom(writer = samPlayer, turnNumber = 1, deadlineEpochMillis = clock.nowEpochMillis + 14_000))
                serverRobot.sendMessage(ServerMessage.TurnExpired(alexPlayer.id))
            }

            THEN("a toast says time is up and the story passes to Sam") {
                playingRobot.assertTimeUpToast("Sam")
            }
        }
}
