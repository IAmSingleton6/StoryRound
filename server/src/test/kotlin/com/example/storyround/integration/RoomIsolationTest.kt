package com.example.storyround.integration

import com.example.storyround.testing.AND
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.gameInPlay
import com.example.storyround.testing.roomInLobby
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class RoomIsolationTest : ServerIntegrationTest() {
    @Test
    fun `should keep each room's messages to itself when two rooms are active`() =
        serverTest {
            GIVEN("Alex is writing in one room while Jo waits in another") {
                gameInPlay(host = "Alex", guests = listOf("Sam"))
                roomInLobby(host = "Jo")
            }

            WHEN("Alex submits a contribution") {
                player("Alex").submitContribution("A horse walked in.")
            }

            THEN("Jo hears nothing about it") {
                player("Jo").assertNoMoreMessages()
            }
        }

    @Test
    fun `should run each room's turn timer separately when two games are in play`() =
        serverTest {
            GIVEN("Jo's solo game started 5 seconds after Alex's") {
                gameInPlay(host = "Alex")
                clock.advanceBy(5.seconds)
                gameInPlay(host = "Jo")
            }

            WHEN("Alex's 15-second turn runs out") {
                clock.advanceBy(10.seconds)
            }

            THEN("Alex's turn expires") {
                player("Alex").assertTurnExpired(of = "Alex")
            }

            AND("Jo still has 5 seconds left") {
                player("Jo").assertTurnNotExpired(of = "Jo")
            }
        }
}
