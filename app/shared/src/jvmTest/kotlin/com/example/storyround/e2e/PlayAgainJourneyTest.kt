package com.example.storyround.e2e

import com.example.storyround.protocol.GameStatus
import com.example.storyround.testing.AND
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.FullStackTest
import com.example.storyround.testing.scenario.appFinishedGame
import kotlin.test.Test

class PlayAgainJourneyTest : FullStackTest() {
    @Test
    fun `should bring everyone back to the lobby when the host plays again`() =
        fullStackTest {
            GIVEN("Alex hosted and finished a game with Sam") {
                appFinishedGame(host = "Alex", guests = listOf("Sam"))
            }

            WHEN("Alex presses play again") {
                finishedRobot.tapPlayAgain()
            }

            THEN("Alex is back in the lobby with the host controls") {
                lobbyRobot.assertHostControlsShown()
                lobbyRobot.assertPlayersShown("Alex", "Sam")
            }

            AND("Sam is back in the lobby too") {
                player("Sam").assertStatus(GameStatus.Lobby)
            }
        }
}
