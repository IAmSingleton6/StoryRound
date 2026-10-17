package com.example.storyround.integration

import com.example.storyround.protocol.GameStatus
import com.example.storyround.random.RandomNumberGenerator
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.gameAwaitingPrompt
import com.example.storyround.testing.roomInLobby
import io.mockk.every
import io.mockk.mockk
import kotlin.random.Random
import kotlin.test.Test

class StartGameTest : ServerIntegrationTest() {
    @Test
    fun `should ask the chosen opener for the prompt when the host starts the game`() =
        serverTest {
            GIVEN("Alex hosts a lobby with Sam") {
                roomInLobby(host = "Alex", guests = listOf("Sam"))
            }

            WHEN("Alex starts a two-round game with Sam opening") {
                player("Alex").startGame(opener = "Sam", rounds = 2)
            }

            THEN("everyone sees the game waiting for Sam's prompt, set to two rounds") {
                player("Sam").assertRoom("Sam opening a two-round game") {
                    it.status == GameStatus.Prompt && it.openingPlayerId == player("Sam").playerId && it.settings.rounds == 2
                }
                player("Alex").assertStatus(GameStatus.Prompt)
            }
        }

    @Test
    fun `should let the server choose the opener when the host picks a random opener`() =
        serverTest(randomNumberGenerator = pickingTheSecondOfTwo()) {
            GIVEN("Alex hosts a lobby with Sam, and the server's random pick lands on the second player") {
                roomInLobby(host = "Alex", guests = listOf("Sam"))
            }

            WHEN("Alex starts the game with a random opener") {
                player("Alex").startGameWithRandomOpener()
            }

            THEN("Sam is asked for the prompt") {
                player("Sam").assertRoom("Sam opening") { it.openingPlayerId == player("Sam").playerId }
            }
        }

    @Test
    fun `should refuse to start when a guest starts the game`() =
        serverTest {
            GIVEN("Alex hosts a lobby with Sam") {
                roomInLobby(host = "Alex", guests = listOf("Sam"))
            }

            WHEN("Sam starts the game") {
                player("Sam").startGame()
            }

            THEN("Sam is told only the host can start") {
                player("Sam").assertErrorReceived("NOT_HOST")
            }
        }

    @Test
    fun `should refuse to start when the rounds are below the minimum`() =
        serverTest {
            GIVEN("Alex hosts a lobby") {
                roomInLobby(host = "Alex")
            }

            WHEN("Alex starts a game with no rounds") {
                player("Alex").startGame(rounds = 0)
            }

            THEN("Alex is told the rounds are invalid") {
                player("Alex").assertErrorReceived("INVALID_ROUNDS")
            }
        }

    @Test
    fun `should refuse to start when the rounds are above the maximum`() =
        serverTest {
            GIVEN("Alex hosts a lobby") {
                roomInLobby(host = "Alex")
            }

            WHEN("Alex starts an eleven-round game") {
                player("Alex").startGame(rounds = 11)
            }

            THEN("Alex is told the rounds are invalid") {
                player("Alex").assertErrorReceived("INVALID_ROUNDS")
            }
        }

    @Test
    fun `should refuse to start when the opener is not in the room`() =
        serverTest {
            GIVEN("Alex hosts a lobby") {
                roomInLobby(host = "Alex")
            }

            WHEN("Alex starts the game with an opener who is not in the room") {
                player("Alex").startGameWithUnknownOpener()
            }

            THEN("Alex is told to choose a player in the room") {
                player("Alex").assertErrorReceived("INVALID_PROMPT_OWNER")
            }
        }

    @Test
    fun `should refuse to start when the game has already started`() =
        serverTest {
            GIVEN("Alex has already started a game") {
                gameAwaitingPrompt(host = "Alex")
            }

            WHEN("Alex starts the game again") {
                player("Alex").startGame()
            }

            THEN("Alex is told the game has started") {
                player("Alex").assertErrorReceived("INVALID_GAME_STATE")
            }
        }

    private fun pickingTheSecondOfTwo(): RandomNumberGenerator =
        mockk {
            every { nextInt(any()) } answers { Random.nextInt(firstArg<Int>()) }
            every { nextInt(2) } returns 1
        }
}
