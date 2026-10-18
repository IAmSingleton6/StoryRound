package com.example.storyround.flow

import com.example.storyround.protocol.ClientMessage
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.AppFlowTest
import com.example.storyround.testing.fixtures.ROOM_CODE
import com.example.storyround.testing.fixtures.alexPlayer
import com.example.storyround.testing.fixtures.finishedRoom
import com.example.storyround.testing.fixtures.lobbyRoom
import com.example.storyround.testing.fixtures.playingRoom
import com.example.storyround.testing.fixtures.tokenOf
import kotlin.test.Test

class LaunchFlowTest : AppFlowTest() {
    @Test
    fun `should show home and send nothing when the app launches with nothing saved`() =
        appFlowTest {
            GIVEN("the server is reachable and the device has no saved session") {
                serverRobot.acceptConnections()
            }

            WHEN("the app launches") {
                appRobot.launch()
            }

            THEN("it connects, shows Home and sends nothing") {
                serverRobot.assertConnectionAttempts(1)
                homeRobot.assertShown()
                serverRobot.assertNothingSent()
            }
        }

    @Test
    fun `should ask to resume the saved session when the app launches`() =
        appFlowTest {
            GIVEN("Alex's session in room 7F2K is saved on the device") {
                storageRobot.saveSession(ROOM_CODE, alexPlayer.id, tokenOf(alexPlayer))
            }

            WHEN("the app launches") {
                appRobot.launch()
            }

            THEN("it asks the server to reconnect Alex") {
                serverRobot.assertSent(ClientMessage.Reconnect(ROOM_CODE, alexPlayer.id, tokenOf(alexPlayer)))
            }
        }

    @Test
    fun `should show the lobby when the server restores a lobby session`() =
        appFlowTest {
            GIVEN("the app launched with Alex's saved session and asked to reconnect") {
                storageRobot.saveSession(ROOM_CODE, alexPlayer.id, tokenOf(alexPlayer))
                appRobot.launch()
                serverRobot.awaitSent(ClientMessage.Reconnect(ROOM_CODE, alexPlayer.id, tokenOf(alexPlayer)))
            }

            WHEN("the server restores Alex to the lobby") {
                serverRobot.resume(alexPlayer, lobbyRoom())
            }

            THEN("Alex is back in the lobby") {
                lobbyRobot.assertRoomCodeShown(ROOM_CODE)
            }
        }

    @Test
    fun `should show the story when the server restores a game in play`() =
        appFlowTest {
            GIVEN("the app launched with Alex's saved session and asked to reconnect") {
                storageRobot.saveSession(ROOM_CODE, alexPlayer.id, tokenOf(alexPlayer))
                appRobot.launch()
                serverRobot.awaitSent(ClientMessage.Reconnect(ROOM_CODE, alexPlayer.id, tokenOf(alexPlayer)))
            }

            WHEN("the server restores Alex to a game where it is Alex's turn") {
                serverRobot.resume(alexPlayer, playingRoom(writer = alexPlayer, deadlineEpochMillis = clock.nowEpochMillis + 15_000))
            }

            THEN("Alex is back on the story with the turn") {
                playingRobot.assertYourTurn()
            }
        }

    @Test
    fun `should show the finished story when the server restores a finished game`() =
        appFlowTest {
            GIVEN("the app launched with Alex's saved session and asked to reconnect") {
                storageRobot.saveSession(ROOM_CODE, alexPlayer.id, tokenOf(alexPlayer))
                appRobot.launch()
                serverRobot.awaitSent(ClientMessage.Reconnect(ROOM_CODE, alexPlayer.id, tokenOf(alexPlayer)))
            }

            WHEN("the server restores Alex to the finished game") {
                serverRobot.resume(alexPlayer, finishedRoom())
            }

            THEN("Alex sees the finished story") {
                finishedRobot.assertStoryShown("A door opened. A horse walked in. It wore a hat.")
            }
        }

    @Test
    fun `should forget the saved session quietly when the server rejects it`() =
        appFlowTest {
            GIVEN("the app launched with a saved session for a room that no longer exists") {
                storageRobot.saveSession(ROOM_CODE, alexPlayer.id, tokenOf(alexPlayer))
                appRobot.launch()
                serverRobot.awaitSent(ClientMessage.Reconnect(ROOM_CODE, alexPlayer.id, tokenOf(alexPlayer)))
            }

            WHEN("the server says the room is gone") {
                serverRobot.refuse("ROOM_NOT_FOUND", "That room does not exist.")
            }

            THEN("the session is forgotten and Home shows no error") {
                storageRobot.assertNoSessionSaved()
                homeRobot.assertNoErrorShown("That room does not exist.")
            }
        }

    @Test
    fun `should explain the server cannot be reached when the launch connection fails`() =
        appFlowTest {
            GIVEN("the server cannot be reached") {
                serverRobot.refuseConnections()
            }

            WHEN("the app launches") {
                appRobot.launch()
            }

            THEN("Home explains it cannot reach Story Round") {
                homeRobot.assertErrorShown("Can't reach Story Round right now. Check your internet connection and try again.")
            }
        }
}
