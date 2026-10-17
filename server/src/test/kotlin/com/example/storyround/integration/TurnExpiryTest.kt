package com.example.storyround.integration

import com.example.storyround.protocol.ServerMessage
import com.example.storyround.testing.AND
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.gameInPlay
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class TurnExpiryTest : ServerIntegrationTest() {
    @Test
    fun `should expire the turn when the deadline is reached`() =
        serverTest {
            GIVEN("Alex is writing the first turn of a game with Sam") {
                gameInPlay(host = "Alex", guests = listOf("Sam"))
            }

            WHEN("the turn's 15 seconds run out") {
                clock.advanceBy(15.seconds)
            }

            THEN("everyone is told Alex's turn expired") {
                player("Alex").assertTurnExpired(of = "Alex")
                player("Sam").assertTurnExpired(of = "Alex")
            }

            AND("Sam's turn starts for everyone") {
                player("Alex").assertTurnStarted(of = "Sam")
                player("Sam").assertTurnStarted(of = "Sam")
            }
        }

    @Test
    fun `should send the room then the expiry then the next turn when a turn expires`() =
        serverTest {
            GIVEN("Alex is writing the first turn of a game with Sam") {
                gameInPlay(host = "Alex", guests = listOf("Sam"))
            }

            WHEN("the turn runs out") {
                clock.advanceBy(15.seconds)
            }

            THEN("Sam receives the updated room, then the expiry, then the next turn") {
                player("Sam").assertNextMessages(
                    ServerMessage.RoomStateChanged::class,
                    ServerMessage.TurnExpired::class,
                    ServerMessage.TurnStarted::class,
                )
            }
        }

    @Test
    fun `should not expire the turn when the writer submits before the deadline`() =
        serverTest {
            GIVEN("Alex has submitted the first turn a moment before its deadline") {
                gameInPlay(host = "Alex", guests = listOf("Sam"), rounds = 2)
                clock.advanceBy(14.seconds)
                player("Alex").submitContribution("Just in time.")
            }

            WHEN("the first turn's deadline passes") {
                clock.advanceBy(1.seconds)
            }

            THEN("Alex's turn does not expire") {
                player("Sam").assertTurnNotExpired(of = "Alex")
            }
        }

    @Test
    fun `should finish the game when the final turn expires`() =
        serverTest {
            GIVEN("Alex is writing the only turn of a solo game") {
                gameInPlay(host = "Alex", prompt = "A door creaked open.")
            }

            WHEN("the turn runs out") {
                clock.advanceBy(15.seconds)
            }

            THEN("the game finishes with only the prompt") {
                player("Alex").assertTurnExpired(of = "Alex")
                player("Alex").assertStoryFinished("A door creaked open.")
            }
        }

    @Test
    fun `should keep expiring turns when the writer has disconnected`() =
        serverTest {
            GIVEN("Alex has disconnected while it is Alex's turn") {
                gameInPlay(host = "Alex", guests = listOf("Sam"))
                player("Alex").disconnect()
            }

            WHEN("Alex's turn runs out") {
                clock.advanceBy(15.seconds + 1.milliseconds)
            }

            THEN("Sam is told Alex's turn expired and it becomes Sam's turn") {
                player("Sam").assertTurnExpired(of = "Alex")
                player("Sam").assertTurnStarted(of = "Sam")
            }
        }
}
