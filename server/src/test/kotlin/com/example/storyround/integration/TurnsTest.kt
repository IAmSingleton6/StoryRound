package com.example.storyround.integration

import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.gameAwaitingPrompt
import com.example.storyround.testing.gameInPlay
import kotlin.test.Test

class TurnsTest : ServerIntegrationTest() {
    @Test
    fun `should pass the turn to the next player in join order when a contribution is submitted`() =
        serverTest {
            GIVEN("Alex is writing the first turn of a game with Sam and Jo") {
                gameInPlay(host = "Alex", guests = listOf("Sam", "Jo"))
            }

            WHEN("Alex submits a contribution") {
                player("Alex").submitContribution("There was a horse in the kitchen.")
            }

            THEN("everyone is told it is Sam's turn") {
                player("Alex").assertTurnStarted(of = "Sam")
                player("Sam").assertTurnStarted(of = "Sam")
                player("Jo").assertTurnStarted(of = "Sam")
            }
        }

    @Test
    fun `should go back to the first player when every player has had a turn in the round`() =
        serverTest {
            GIVEN("Sam is writing the last turn of the first of two rounds") {
                gameInPlay(host = "Alex", guests = listOf("Sam"), rounds = 2)
                player("Alex").submitContribution("Line 1.")
            }

            WHEN("Sam submits a contribution") {
                player("Sam").submitContribution("Line 2.")
            }

            THEN("the second round starts with Alex") {
                player("Sam").assertCurrentTurn(of = "Alex", number = 2)
            }
        }

    @Test
    fun `should send the room then the contribution then the next turn when a contribution is submitted`() =
        serverTest {
            GIVEN("Alex is writing the first turn of a game with Sam") {
                gameInPlay(host = "Alex", guests = listOf("Sam"))
            }

            WHEN("Alex submits a contribution") {
                player("Alex").submitContribution("A horse walked in.")
            }

            THEN("Sam receives the updated room, then the contribution, then the next turn") {
                player("Sam").assertNextMessages(
                    ServerMessage.RoomStateChanged::class,
                    ServerMessage.ContributionAdded::class,
                    ServerMessage.TurnStarted::class,
                )
            }
        }

    @Test
    fun `should make the next turn a second shorter when a turn is submitted`() =
        serverTest {
            GIVEN("Alex is writing the 15-second first turn of a solo game") {
                gameInPlay(host = "Alex", rounds = 3)
            }

            WHEN("Alex submits the first turn") {
                player("Alex").submitContribution("Line 1.")
            }

            THEN("the second turn lasts 14 seconds") {
                player("Alex").assertCurrentTurn(of = "Alex", number = 1, endingAt = clock.nowEpochMillis + 14_000)
            }
        }

    @Test
    fun `should stop shortening turns when they reach the minimum length`() =
        serverTest(configuration = GameConfiguration(firstTurnMillis = 4_000, turnReductionMillis = 1_000, minimumTurnMillis = 2_000)) {
            GIVEN("Alex has written the 4-second and 3-second turns of a solo game, and the minimum turn is 2 seconds") {
                gameInPlay(host = "Alex", rounds = 4)
                player("Alex").submitContribution("Line 1.")
                player("Alex").submitContribution("Line 2.")
            }

            WHEN("Alex submits the 2-second third turn") {
                player("Alex").submitContribution("Line 3.")
            }

            THEN("the fourth turn also lasts 2 seconds") {
                player("Alex").assertCurrentTurn(of = "Alex", number = 3, endingAt = clock.nowEpochMillis + 2_000)
            }
        }

    @Test
    fun `should refuse a contribution when it is not the player's turn`() =
        serverTest {
            GIVEN("Alex is writing the first turn of a game with Sam") {
                gameInPlay(host = "Alex", guests = listOf("Sam"))
            }

            WHEN("Sam submits a contribution") {
                player("Sam").trySubmitContribution("Trying to skip ahead.")
            }

            THEN("Sam is told it is not Sam's turn") {
                player("Sam").assertErrorReceived("NOT_CURRENT_PLAYER")
            }
        }

    @Test
    fun `should refuse a contribution when it is blank`() =
        serverTest {
            GIVEN("Alex is writing the first turn") {
                gameInPlay(host = "Alex")
            }

            WHEN("Alex submits only spaces") {
                player("Alex").submitContribution("   ")
            }

            THEN("Alex is told the text is invalid") {
                player("Alex").assertErrorReceived("INVALID_TEXT")
            }
        }

    @Test
    fun `should refuse a contribution when the game is waiting for the prompt`() =
        serverTest {
            GIVEN("Alex is writing the opening prompt") {
                gameAwaitingPrompt(host = "Alex")
            }

            WHEN("Alex submits a contribution instead") {
                player("Alex").trySubmitContribution("Too soon.")
            }

            THEN("Alex is told the game is not taking contributions") {
                player("Alex").assertErrorReceived("INVALID_GAME_STATE")
            }
        }

    @Test
    fun `should give the only player every turn when playing solo`() =
        serverTest {
            GIVEN("Alex is writing the first turn of a two-round solo game") {
                gameInPlay(host = "Alex", rounds = 2)
            }

            WHEN("Alex submits the first turn") {
                player("Alex").submitContribution("Line 1.")
            }

            THEN("it is Alex's turn again") {
                player("Alex").assertCurrentTurn(of = "Alex", number = 1)
            }
        }
}
