package com.example.storyround.integration

import com.example.storyround.protocol.GameStatus
import com.example.storyround.testing.AND
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.gameAwaitingPrompt
import kotlin.test.Test
import kotlin.time.Duration.Companion.hours

class OpeningPromptTest : ServerIntegrationTest() {
    @Test
    fun `should start the first timed turn when the opener submits the prompt`() =
        serverTest {
            GIVEN("Sam is writing the opening prompt of a game with Alex") {
                gameAwaitingPrompt(host = "Alex", guests = listOf("Sam"), opener = "Sam")
            }

            WHEN("Sam submits the prompt") {
                player("Sam").submitPrompt("A door opened.")
            }

            THEN("everyone sees the story begin with Sam's prompt") {
                player("Alex").assertRoom("the story playing from Sam's prompt") {
                    it.status == GameStatus.Playing && it.prompt == "A door opened."
                }
            }

            AND("Alex, first to join, gets the first 15-second turn") {
                player("Alex").assertCurrentTurn(of = "Alex", number = 0, endingAt = clock.nowEpochMillis + 15_000)
                player("Sam").assertTurnStarted(of = "Alex")
            }
        }

    @Test
    fun `should refuse the prompt when someone other than the opener submits it`() =
        serverTest {
            GIVEN("Sam is writing the opening prompt of a game with Alex") {
                gameAwaitingPrompt(host = "Alex", guests = listOf("Sam"), opener = "Sam")
            }

            WHEN("Alex submits a prompt instead") {
                player("Alex").trySubmitPrompt("My door, actually.")
            }

            THEN("Alex is told only the opener can start the story") {
                player("Alex").assertErrorReceived("NOT_PROMPT_OWNER")
            }
        }

    @Test
    fun `should refuse the prompt when it is blank`() =
        serverTest {
            GIVEN("Alex is writing the opening prompt") {
                gameAwaitingPrompt(host = "Alex")
            }

            WHEN("Alex submits only spaces") {
                player("Alex").submitPrompt("   ")
            }

            THEN("Alex is told the text is invalid") {
                player("Alex").assertErrorReceived("INVALID_TEXT")
            }
        }

    @Test
    fun `should refuse the prompt when it is longer than 280 characters`() =
        serverTest {
            GIVEN("Alex is writing the opening prompt") {
                gameAwaitingPrompt(host = "Alex")
            }

            WHEN("Alex submits 281 characters") {
                player("Alex").submitPrompt("a".repeat(281))
            }

            THEN("Alex is told the text is invalid") {
                player("Alex").assertErrorReceived("INVALID_TEXT")
            }
        }

    @Test
    fun `should tidy the prompt's spacing when it is submitted`() =
        serverTest {
            GIVEN("Alex is writing the opening prompt") {
                gameAwaitingPrompt(host = "Alex")
            }

            WHEN("Alex submits a prompt with extra spaces and line breaks") {
                player("Alex").submitPrompt("  A  door\n\nopened.  ")
            }

            THEN("the story starts with the tidied prompt") {
                player("Alex").assertRoom("the prompt \"A door opened.\"") { it.prompt == "A door opened." }
            }
        }

    @Test
    fun `should not run a timer when the game is waiting for the prompt`() =
        serverTest {
            GIVEN("Alex is writing the opening prompt of a game with Sam") {
                gameAwaitingPrompt(host = "Alex", guests = listOf("Sam"))
            }

            WHEN("an hour passes") {
                clock.advanceBy(1.hours)
            }

            THEN("nothing happens") {
                player("Alex").assertNoMoreMessages()
                player("Sam").assertNoMoreMessages()
            }
        }
}
