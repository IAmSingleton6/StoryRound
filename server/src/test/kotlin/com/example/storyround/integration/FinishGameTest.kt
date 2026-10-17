package com.example.storyround.integration

import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.gameFinished
import com.example.storyround.testing.gameInPlay
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

class FinishGameTest : ServerIntegrationTest() {
    @Test
    fun `should send everyone the whole story when the last turn is submitted`() =
        serverTest {
            GIVEN("Sam is writing the last turn of a one-round game with Alex") {
                gameInPlay(host = "Alex", guests = listOf("Sam"), prompt = "A door creaked open.")
                player("Alex").submitContribution("A horse walked in.")
            }

            WHEN("Sam submits the last turn") {
                player("Sam").submitContribution("It wore a hat.")
            }

            THEN("everyone receives the prompt and both contributions, in order") {
                player("Alex").assertStoryFinished("A door creaked open.", "A horse walked in.", "It wore a hat.")
                player("Sam").assertStoryFinished("A door creaked open.", "A horse walked in.", "It wore a hat.")
            }
        }

    @Test
    fun `should stop the turn timer when the game finishes`() =
        serverTest {
            GIVEN("Alex and Sam have finished a game") {
                gameFinished(host = "Alex", guests = listOf("Sam"))
            }

            WHEN("a minute passes") {
                clock.advanceBy(1.minutes)
            }

            THEN("no turn expires and nothing else is sent") {
                player("Alex").assertNoMoreMessages()
                player("Sam").assertNoMoreMessages()
            }
        }
}
