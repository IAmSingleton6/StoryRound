package com.example.storyround.flow

import com.example.storyround.protocol.ClientMessage
import com.example.storyround.testing.AND
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.AppFlowTest
import com.example.storyround.testing.fixtures.ROOM_CODE
import com.example.storyround.testing.fixtures.alexPlayer
import com.example.storyround.testing.fixtures.lobbyRoom
import com.example.storyround.testing.fixtures.samPlayer
import com.example.storyround.testing.fixtures.tokenOf
import com.example.storyround.testing.scenario.appOnHome
import kotlin.test.Test

class CreateAndJoinFlowTest : AppFlowTest() {
    @Test
    fun `should ask the server for a room when the player presses create`() =
        appFlowTest {
            GIVEN("Alex has typed their name on Home") {
                appOnHome()
                homeRobot.enterName("Alex")
            }

            WHEN("Alex presses create") {
                homeRobot.tapCreateGame()
            }

            THEN("the app asks the server to create a room for Alex") {
                serverRobot.assertSent(ClientMessage.CreateRoom("Alex"))
            }
        }

    @Test
    fun `should show the lobby as host and save the session when the server creates the room`() =
        appFlowTest {
            GIVEN("Alex has asked to create a room") {
                appOnHome()
                homeRobot.enterName("Alex")
                homeRobot.tapCreateGame()
                serverRobot.awaitSent(ClientMessage.CreateRoom("Alex"))
            }

            WHEN("the server creates room 7F2K with Alex as host") {
                serverRobot.admit(alexPlayer, lobbyRoom())
            }

            THEN("Alex is in the lobby with the host controls") {
                lobbyRobot.assertRoomCodeShown(ROOM_CODE)
                lobbyRobot.assertHostControlsShown()
            }

            AND("the session is saved on the device") {
                storageRobot.assertSessionSaved(ROOM_CODE, alexPlayer.id)
            }
        }

    @Test
    fun `should connect again and create without resuming the old session when the launch connection failed`() =
        appFlowTest {
            GIVEN("the server was unreachable at launch but is back, and Alex has an old saved session and has typed their name") {
                storageRobot.saveSession("OLD1", alexPlayer.id, tokenOf(alexPlayer))
                serverRobot.refuseConnections()
                appRobot.launch()
                homeRobot.assertErrorShown("Can't reach Story Round right now. Check your internet connection and try again.")
                serverRobot.acceptConnections()
                homeRobot.enterName("Alex")
            }

            WHEN("Alex presses create") {
                homeRobot.tapCreateGame()
            }

            THEN("the app connects and asks to create a room") {
                serverRobot.assertSent(ClientMessage.CreateRoom("Alex"))
            }

            AND("it does not try to resume the old session") {
                serverRobot.assertNothingSent()
            }
        }

    @Test
    fun `should send the room code in capitals when the player types it in lower case`() =
        appFlowTest {
            GIVEN("Sam has typed their name and a lower-case room code in join mode") {
                appOnHome()
                homeRobot.enterName("Sam")
                homeRobot.tapJoinGame()
                homeRobot.enterRoomCode("7f2k")
            }

            WHEN("Sam presses join") {
                homeRobot.tapJoinGame()
            }

            THEN("the app asks to join room 7F2K as Sam") {
                serverRobot.assertSent(ClientMessage.JoinRoom(ROOM_CODE, "Sam"))
            }
        }

    @Test
    fun `should show the lobby as a guest when the server accepts the join`() =
        appFlowTest {
            GIVEN("Sam has asked to join room 7F2K") {
                appOnHome()
                homeRobot.enterName("Sam")
                homeRobot.tapJoinGame()
                homeRobot.enterRoomCode(ROOM_CODE)
                homeRobot.tapJoinGame()
                serverRobot.awaitSent(ClientMessage.JoinRoom(ROOM_CODE, "Sam"))
            }

            WHEN("the server seats Sam in Alex's lobby") {
                serverRobot.admit(samPlayer, lobbyRoom(players = listOf(alexPlayer, samPlayer)))
            }

            THEN("Sam waits for Alex to start") {
                lobbyRobot.assertWaitingForHost("Alex")
            }
        }

    @Test
    fun `should stay on home with the server's message when the join is refused`() =
        appFlowTest {
            GIVEN("Sam has asked to join room ZZZZ") {
                appOnHome()
                homeRobot.enterName("Sam")
                homeRobot.tapJoinGame()
                homeRobot.enterRoomCode("ZZZZ")
                homeRobot.tapJoinGame()
                serverRobot.awaitSent(ClientMessage.JoinRoom("ZZZZ", "Sam"))
            }

            WHEN("the server says there is no such room") {
                serverRobot.refuse("ROOM_NOT_FOUND", "No game found with that code. Check it and try again.")
            }

            THEN("Home shows the message and Sam can try again") {
                homeRobot.assertErrorShown("No game found with that code. Check it and try again.")
                homeRobot.assertJoinEnabled(true)
            }
        }
}
