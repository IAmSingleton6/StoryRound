package com.example.storyround.e2e

import com.example.storyround.testing.AND
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.FullStackTest
import com.example.storyround.testing.scenario.appJoinedLobby
import kotlin.test.Test

class JoinJourneyTest : FullStackTest() {
    @Test
    fun `should join a friend's room when the player types its code`() =
        fullStackTest {
            GIVEN("Sam is hosting a room and Alex has typed its code in the app") {
                player("Sam").createRoom()
                appRobot.launch()
                homeRobot.enterName("Alex")
                homeRobot.tapJoinGame()
                homeRobot.enterRoomCode(player("Sam").roomCode)
            }

            WHEN("Alex presses join") {
                homeRobot.tapJoinGame()
            }

            THEN("Alex waits in Sam's lobby") {
                lobbyRobot.assertWaitingForHost("Sam")
            }

            AND("Sam is told Alex joined") {
                player("Sam").assertPlayerJoined("Alex")
            }
        }

    @Test
    fun `should send the app's opening prompt to the other players when the app opens the story`() =
        fullStackTest {
            GIVEN("Alex joined Sam's room and Sam started the game with Alex opening") {
                appJoinedLobby(guest = "Alex", host = "Sam")
                player("Sam").startGame(opener = "Alex")
                openingPromptRobot.enterPrompt("A door opened.")
            }

            WHEN("Alex starts the story") {
                openingPromptRobot.tapStartStory()
            }

            THEN("Sam's story begins with Alex's prompt") {
                player("Sam").assertRoom("the story opening with Alex's prompt") { it.prompt == "A door opened." }
            }
        }
}
