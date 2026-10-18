package com.example.storyround.flow

import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.Contribution
import com.example.storyround.testing.AND
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.AppFlowTest
import com.example.storyround.testing.fixtures.ROOM_CODE
import com.example.storyround.testing.fixtures.alexPlayer
import com.example.storyround.testing.fixtures.finishedRoom
import com.example.storyround.testing.fixtures.lobbyRoom
import com.example.storyround.testing.fixtures.playingRoom
import com.example.storyround.testing.fixtures.samPlayer
import com.example.storyround.testing.fixtures.tokenOf
import com.example.storyround.testing.scenario.appResumedInto
import kotlin.test.Test

class FinishFlowTest : AppFlowTest() {
    @Test
    fun `should show the finished story and save it on the device when the game finishes`() =
        appFlowTest {
            GIVEN("Sam is writing the last turn after Alex's contribution") {
                val alexTurn = Contribution(alexPlayer.id, "A horse walked in.", turnNumber = 0)
                appResumedInto(
                    playingRoom(
                        writer = samPlayer,
                        turnNumber = 1,
                        contributions = listOf(alexTurn),
                        deadlineEpochMillis = clock.nowEpochMillis + 14_000,
                    ),
                    alexPlayer,
                )
            }

            WHEN("the server finishes the game") {
                serverRobot.finishGame(finishedRoom())
            }

            THEN("Alex sees the whole story") {
                finishedRobot.assertStoryShown("A door opened. A horse walked in. It wore a hat.")
            }

            AND("the story is saved on the device") {
                storageRobot.assertHistory("A door opened.")
            }
        }

    @Test
    fun `should count the new story on home when the player goes home`() =
        appFlowTest {
            GIVEN("Alex is looking at a finished story") {
                appResumedInto(finishedRoom(), alexPlayer)
                finishedRobot.assertShown()
            }

            WHEN("Alex goes home") {
                finishedRobot.tapHome()
            }

            THEN("Home counts one saved story") {
                homeRobot.assertHistoryCount("1 saved story")
            }
        }

    @Test
    fun `should keep the session without leaving the room when the player goes home`() =
        appFlowTest {
            GIVEN("Alex is looking at a finished story") {
                appResumedInto(finishedRoom(), alexPlayer)
                finishedRobot.assertShown()
            }

            WHEN("Alex goes home") {
                finishedRobot.tapHome()
            }

            THEN("the app does not tell the server Alex left") {
                serverRobot.assertNothingSent()
            }

            AND("the session stays saved, so the next launch can resume it") {
                storageRobot.assertSessionSaved(ROOM_CODE, alexPlayer.id)
            }
        }

    @Test
    fun `should ask to play again when the host presses play again`() =
        appFlowTest {
            GIVEN("Alex, the host, is looking at a finished story") {
                appResumedInto(finishedRoom(), alexPlayer)
            }

            WHEN("Alex presses play again") {
                finishedRobot.tapPlayAgain()
            }

            THEN("the app asks the server to play again") {
                serverRobot.assertSent(ClientMessage.PlayAgain(ROOM_CODE, alexPlayer.id))
            }
        }

    @Test
    fun `should return to the lobby when the server resets the room`() =
        appFlowTest {
            GIVEN("Alex, the host, is looking at a finished story") {
                appResumedInto(finishedRoom(), alexPlayer)
                finishedRobot.assertShown()
            }

            WHEN("the server resets the room for another game") {
                serverRobot.sendRoom(lobbyRoom(players = listOf(alexPlayer, samPlayer)))
            }

            THEN("Alex is back in the lobby with the host controls") {
                lobbyRobot.assertHostControlsShown()
            }
        }

    @Test
    fun `should save a finished story only once when it is sent again on reconnect`() =
        appFlowTest {
            GIVEN("Alex is looking at a finished story that has been saved") {
                appResumedInto(finishedRoom(), alexPlayer)
                storageRobot.assertHistory("A door opened.")
            }

            WHEN("the connection drops and the server sends the story again on reconnect") {
                serverRobot.dropConnection()
                serverRobot.awaitSent(ClientMessage.Reconnect(ROOM_CODE, alexPlayer.id, tokenOf(alexPlayer)))
                serverRobot.resume(alexPlayer, finishedRoom())
                serverRobot.awaitAppCaughtUp()
            }

            THEN("the device still holds one copy of the story") {
                storageRobot.assertHistory("A door opened.")
            }
        }
}
