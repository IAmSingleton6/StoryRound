package com.example.storyround.integration

import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.testing.AND
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.gameFinished
import com.example.storyround.testing.gameInPlay
import kotlin.test.Test

class PlayAgainTest : ServerIntegrationTest() {
    @Test
    fun `should return everyone to an empty lobby when the host plays again`() =
        serverTest {
            GIVEN("Alex and Sam have finished a game") {
                gameFinished(host = "Alex", guests = listOf("Sam"))
            }

            WHEN("Alex plays again") {
                player("Alex").playAgain()
            }

            THEN("Sam sees the lobby again with no story") {
                player("Sam").assertRoom("the lobby with no story") {
                    it.status == GameStatus.Lobby && it.prompt == null && it.contributions.isEmpty()
                }
            }

            AND("the same players keep the same colours") {
                player("Sam").assertPlayers("Alex", "Sam")
                player("Sam").assertColourOf("Sam", PlayerColour.Green)
            }
        }

    @Test
    fun `should refuse to play again when a guest asks`() =
        serverTest {
            GIVEN("Alex and Sam have finished a game") {
                gameFinished(host = "Alex", guests = listOf("Sam"))
            }

            WHEN("Sam plays again") {
                player("Sam").playAgain()
            }

            THEN("Sam is told only the host can start another game") {
                player("Sam").assertErrorReceived("NOT_HOST")
            }
        }

    @Test
    fun `should refuse to play again when the game has not finished`() =
        serverTest {
            GIVEN("Alex is still writing the first turn") {
                gameInPlay(host = "Alex")
            }

            WHEN("Alex asks to play again") {
                player("Alex").tryPlayAgain()
            }

            THEN("Alex is told to finish the current game first") {
                player("Alex").assertErrorReceived("INVALID_GAME_STATE")
            }
        }

    @Test
    fun `should start a new game normally when the room has been reset`() =
        serverTest {
            GIVEN("Alex and Sam are back in the lobby after a finished game") {
                gameFinished(host = "Alex", guests = listOf("Sam"))
                player("Alex").playAgain()
            }

            WHEN("Alex starts a new game with Sam opening") {
                player("Alex").startGame(opener = "Sam")
            }

            THEN("Sam is asked for a new prompt") {
                player("Sam").assertRoom("Sam opening a new game") {
                    it.status == GameStatus.Prompt && it.openingPlayerId == player("Sam").playerId
                }
            }
        }
}
