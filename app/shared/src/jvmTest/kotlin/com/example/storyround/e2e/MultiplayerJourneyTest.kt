package com.example.storyround.e2e

import com.example.storyround.testing.AND
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.FullStackTest
import com.example.storyround.testing.scenario.appHostingGame
import com.example.storyround.testing.scenario.appHostingLobby
import kotlin.test.Test

class MultiplayerJourneyTest : FullStackTest() {
    @Test
    fun `should show a friend's seat when they join the lobby`() =
        fullStackTest {
            GIVEN("Alex is hosting a lobby from the app") {
                appHostingLobby(host = "Alex")
            }

            WHEN("Sam joins with the room code shown in the app") {
                player("Sam").joinRoom(lobbyRobot.readRoomCode())
            }

            THEN("the lobby shows both players") {
                lobbyRobot.assertPlayersShown("Alex", "Sam")
            }
        }

    @Test
    fun `should show the other player's prompt when they open the story`() =
        fullStackTest {
            GIVEN("Alex started a game in which Sam writes the opening") {
                appHostingGame(host = "Alex", guests = listOf("Sam"), opener = "Sam")
            }

            WHEN("Sam submits the prompt") {
                player("Sam").submitPrompt("A door opened.")
            }

            THEN("Alex sees the story begin with Sam's prompt, and it is Alex's turn") {
                playingRobot.assertStoryShown("A door opened.")
                playingRobot.assertYourTurn()
            }
        }

    @Test
    fun `should show the finished story when the last contribution is submitted`() =
        fullStackTest {
            GIVEN("Alex and Sam are one contribution from the end of a one-round game") {
                appHostingGame(host = "Alex", guests = listOf("Sam"), opener = "Sam")
                player("Sam").submitPrompt("A door opened.")
                playingRobot.enterContribution("A horse walked in.")
                playingRobot.tapSubmit()
            }

            WHEN("Sam submits the final contribution") {
                player("Sam").submitContribution("It wore a hat.")
            }

            THEN("Alex sees the whole story on the finished screen") {
                finishedRobot.assertStoryShown("A door opened. A horse walked in. It wore a hat.")
            }

            AND("Sam received the same story") {
                player("Sam").assertStoryFinished("A door opened.", "A horse walked in.", "It wore a hat.")
            }
        }
}
