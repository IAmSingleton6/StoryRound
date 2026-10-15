package com.example.storyround.ui.components

import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.ComponentTest
import kotlin.test.Test

class WritingFieldTest :
    ComponentTest<WritingFieldTest.Draft, WritingFieldTest.WritingEvent>({ draft, onEvent ->
        WritingField(
            value = draft.text,
            onValueChange = { onEvent(WritingFieldTest.WritingEvent.Typed(it)) },
            placeholder = PLACEHOLDER,
            maxLength = MAX_LENGTH,
            sendLabel = SEND,
            canSend = draft.canSend,
            onSend = { onEvent(WritingFieldTest.WritingEvent.Sent) },
            onRandomIdea = { onEvent(WritingFieldTest.WritingEvent.IdeaRequested) },
        )
    }) {
    @Test
    fun `should count the characters against the limit when text has been written`() =
        componentTest {
            GIVEN("six characters have been written") {
                useState(Draft(text = "A door"))
            }

            WHEN("the writing field is shown") {
                show()
            }

            THEN("the counter reads 6 of 280") {
                writingFieldRobot.assertCounter("6/280")
            }
        }

    @Test
    fun `should report the typed text when the player types`() =
        componentTest {
            GIVEN("an empty writing field is showing") {
                useState(Draft())
                show()
            }

            WHEN("the player types \"A door opened.\"") {
                writingFieldRobot.enterText("A door opened.")
            }

            THEN("the typed text is reported") {
                actions.assertEmitted(WritingEvent.Typed("A door opened."))
            }
        }

    @Test
    fun `should send when the send button is pressed and sending is allowed`() =
        componentTest {
            GIVEN("a writing field with a draft that can be sent") {
                useState(Draft(text = "A door opened.", canSend = true))
                show()
            }

            WHEN("the player presses send") {
                writingFieldRobot.tapSend(SEND)
            }

            THEN("the draft is sent") {
                actions.assertEmitted(WritingEvent.Sent)
            }
        }

    @Test
    fun `should disable sending when sending is not allowed`() =
        componentTest {
            GIVEN("sending is not allowed") {
                useState(Draft(canSend = false))
            }

            WHEN("the writing field is shown") {
                show()
            }

            THEN("the send button is disabled") {
                writingFieldRobot.assertSendEnabled(SEND, enabled = false)
            }
        }

    @Test
    fun `should ask for an idea when idea is pressed`() =
        componentTest {
            GIVEN("an empty writing field is showing") {
                useState(Draft())
                show()
            }

            WHEN("the player presses idea") {
                writingFieldRobot.tapIdea()
            }

            THEN("an idea is requested") {
                actions.assertEmitted(WritingEvent.IdeaRequested)
            }
        }

    @Test
    fun `should label the field with its placeholder when shown`() =
        componentTest {
            GIVEN("an empty draft") {
                useState(Draft(text = ""))
            }

            WHEN("the writing field is shown") {
                show()
            }

            THEN("screen readers hear the placeholder as its label") {
                writingFieldRobot.assertLabelled(PLACEHOLDER)
            }
        }

    @Test
    fun `should take focus when the field appears`() =
        componentTest {
            GIVEN("an empty draft") {
                useState(Draft(text = ""))
            }

            WHEN("the writing field appears") {
                show()
            }

            THEN("it has focus, ready for typing") {
                writingFieldRobot.assertFocused()
            }
        }

    data class Draft(
        val text: String = "",
        val canSend: Boolean = false,
    )

    sealed interface WritingEvent {
        data class Typed(
            val text: String,
        ) : WritingEvent

        data object Sent : WritingEvent

        data object IdeaRequested : WritingEvent
    }

    private companion object {
        const val SEND = "SUBMIT"
        const val PLACEHOLDER = "Continue the story…"
        const val MAX_LENGTH = 280
    }
}
