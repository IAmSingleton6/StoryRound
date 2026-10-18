package com.example.storyround.flow

import com.example.storyround.protocol.ClientMessage
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.AppFlowTest
import com.example.storyround.testing.fixtures.ROOM_CODE
import com.example.storyround.testing.fixtures.alexPlayer
import com.example.storyround.testing.fixtures.lobbyRoom
import com.example.storyround.testing.fixtures.playingRoom
import com.example.storyround.testing.fixtures.samPlayer
import com.example.storyround.testing.fixtures.tokenOf
import com.example.storyround.testing.scenario.appOnHome
import com.example.storyround.testing.scenario.appResumedInto
import kotlin.test.Test

class ConnectionFlowTest : AppFlowTest() {
    @Test
    fun `should show the reconnecting banner when the connection drops in the lobby`() =
        appFlowTest {
            GIVEN("Alex is in the lobby") {
                appResumedInto(lobbyRoom(), alexPlayer)
                lobbyRobot.assertShown()
            }

            WHEN("the connection drops") {
                serverRobot.dropConnection()
            }

            THEN("the lobby says Alex's seat is saved while reconnecting") {
                lobbyRobot.assertReconnectingShown()
            }
        }

    @Test
    fun `should show the reconnecting banner when the connection drops during a game`() =
        appFlowTest {
            GIVEN("Sam is watching Alex's turn") {
                appResumedInto(playingRoom(writer = alexPlayer, deadlineEpochMillis = clock.nowEpochMillis + 15_000), samPlayer)
                playingRobot.assertTurnOf("Alex")
            }

            WHEN("the connection drops") {
                serverRobot.dropConnection()
            }

            THEN("the story says Sam's seat is saved while reconnecting") {
                playingRobot.assertReconnectingShown()
            }
        }

    @Test
    fun `should resume the seat and clear the banner when the app reconnects`() =
        appFlowTest {
            GIVEN("Alex's connection to the lobby dropped and the app has asked to reconnect") {
                appResumedInto(lobbyRoom(), alexPlayer)
                lobbyRobot.assertShown()
                serverRobot.dropConnection()
                serverRobot.awaitSent(ClientMessage.Reconnect(ROOM_CODE, alexPlayer.id, tokenOf(alexPlayer)))
            }

            WHEN("the server restores Alex's seat") {
                serverRobot.resume(alexPlayer, lobbyRoom())
            }

            THEN("the reconnecting banner goes away") {
                lobbyRobot.assertReconnectingNotShown()
            }
        }

    @Test
    fun `should stop reconnecting when the player leaves the lobby while disconnected`() =
        appFlowTest {
            GIVEN("Alex's connection to the lobby dropped and the server cannot be reached") {
                appResumedInto(lobbyRoom(), alexPlayer)
                lobbyRobot.assertShown()
                serverRobot.refuseConnections()
                serverRobot.dropConnection()
                lobbyRobot.assertReconnectingShown()
            }

            WHEN("Alex leaves the lobby") {
                lobbyRobot.tapLeave()
            }

            THEN("Alex is home and the app stops trying to reconnect") {
                homeRobot.assertShown()
                serverRobot.assertNoNewConnectionAttempts()
            }
        }

    @Test
    fun `should show an error on home when the server sends something unreadable`() =
        appFlowTest {
            GIVEN("the app is on Home") {
                appOnHome()
            }

            WHEN("the server sends a frame the app cannot read") {
                serverRobot.sendUnreadableFrame()
            }

            THEN("Home shows that the message could not be read") {
                homeRobot.assertErrorShown("Received an unreadable server message.")
            }
        }
}
