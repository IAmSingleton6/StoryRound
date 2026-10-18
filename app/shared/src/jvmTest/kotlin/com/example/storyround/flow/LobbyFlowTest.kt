package com.example.storyround.flow

import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.RoomSettings
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.testing.AND
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.AppFlowTest
import com.example.storyround.testing.fixtures.ROOM_CODE
import com.example.storyround.testing.fixtures.alexPlayer
import com.example.storyround.testing.fixtures.lobbyRoom
import com.example.storyround.testing.fixtures.promptRoom
import com.example.storyround.testing.fixtures.samPlayer
import com.example.storyround.testing.scenario.appResumedInto
import kotlin.test.Test

class LobbyFlowTest : AppFlowTest() {
    @Test
    fun `should show a newcomer's seat when another player joins`() =
        appFlowTest {
            GIVEN("Alex is alone in the lobby Alex hosts") {
                appResumedInto(lobbyRoom(), alexPlayer)
            }

            WHEN("the server reports Sam joining") {
                serverRobot.sendRoom(lobbyRoom(players = listOf(alexPlayer, samPlayer)))
                serverRobot.sendMessage(ServerMessage.PlayerJoined(samPlayer))
            }

            THEN("Sam has a seat next to Alex") {
                lobbyRobot.assertPlayersShown("Alex", "Sam")
            }
        }

    @Test
    fun `should send the host's choices when the host starts the game`() =
        appFlowTest {
            GIVEN("Alex hosts a lobby with Sam and has chosen Sam to open four rounds") {
                appResumedInto(lobbyRoom(players = listOf(alexPlayer, samPlayer)), alexPlayer)
                lobbyRobot.selectOpener("Sam")
                lobbyRobot.tapMoreRounds()
            }

            WHEN("Alex presses start") {
                lobbyRobot.tapStartGame()
            }

            THEN("the app asks to start with Sam opening four rounds") {
                serverRobot.assertSent(ClientMessage.StartGame(ROOM_CODE, alexPlayer.id, samPlayer.id, settings = RoomSettings(rounds = 4)))
            }
        }

    @Test
    fun `given the host turned on one word each when the host starts the game then one word turns are requested`() =
        appFlowTest {
            GIVEN("Alex hosts a lobby with Sam and has turned on one word each") {
                appResumedInto(lobbyRoom(players = listOf(alexPlayer, samPlayer)), alexPlayer)
                lobbyRobot.tapOneWordEach()
            }

            WHEN("Alex presses start") {
                lobbyRobot.tapStartGame()
            }

            THEN("the app asks to start a one word game") {
                serverRobot.assertSent(
                    ClientMessage.StartGame(ROOM_CODE, alexPlayer.id, null, settings = RoomSettings(rounds = 3, oneWordPerTurn = true)),
                )
            }
        }

    @Test
    fun `should ask the opener for the prompt when the game starts`() =
        appFlowTest {
            GIVEN("Alex hosts a lobby with Sam") {
                appResumedInto(lobbyRoom(players = listOf(alexPlayer, samPlayer)), alexPlayer)
            }

            WHEN("the server starts the game with Alex opening") {
                serverRobot.sendRoom(promptRoom(opener = alexPlayer))
            }

            THEN("Alex is asked for the opening prompt") {
                openingPromptRobot.assertAskedForPrompt()
            }
        }

    @Test
    fun `should show who is opening when someone else opens`() =
        appFlowTest {
            GIVEN("Sam is a guest in Alex's lobby") {
                appResumedInto(lobbyRoom(players = listOf(alexPlayer, samPlayer)), samPlayer)
            }

            WHEN("the server starts the game with Alex opening") {
                serverRobot.sendRoom(promptRoom(opener = alexPlayer))
            }

            THEN("Sam waits for Alex's prompt") {
                openingPromptRobot.assertWaitingFor("Alex")
            }
        }

    @Test
    fun `should tell the server and forget the session when a player leaves the lobby`() =
        appFlowTest {
            GIVEN("Sam is a guest in Alex's lobby") {
                appResumedInto(lobbyRoom(players = listOf(alexPlayer, samPlayer)), samPlayer)
            }

            WHEN("Sam leaves the lobby") {
                lobbyRobot.tapLeave()
            }

            THEN("the app tells the server Sam left") {
                serverRobot.assertSent(ClientMessage.LeaveRoom(ROOM_CODE, samPlayer.id))
            }

            AND("Sam is home with no saved session") {
                homeRobot.assertShown()
                storageRobot.assertNoSessionSaved()
            }
        }
}
