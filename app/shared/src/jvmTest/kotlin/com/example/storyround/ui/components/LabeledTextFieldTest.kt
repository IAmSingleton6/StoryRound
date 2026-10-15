package com.example.storyround.ui.components

import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.ComponentTest
import kotlin.test.Test

class LabeledTextFieldTest :
    ComponentTest<String, String>({ value, onChange ->
        LabeledTextField(value, onChange, label = LABEL, placeholder = PLACEHOLDER)
    }) {
    @Test
    fun `should show its label in capitals and use it for screen readers when shown`() =
        componentTest {
            GIVEN("a field labelled \"Your name\"") {
                useState("")
            }

            WHEN("it is shown") {
                show()
            }

            THEN("the label reads YOUR NAME and screen readers hear \"Your name\"") {
                textFieldRobot.assertLabelShown("YOUR NAME")
                textFieldRobot.assertLabelled(LABEL)
            }
        }

    @Test
    fun `should show the placeholder when the field is empty`() =
        componentTest {
            GIVEN("the field is empty") {
                useState("")
            }

            WHEN("it is shown") {
                show()
            }

            THEN("the placeholder is shown") {
                textFieldRobot.assertPlaceholderShown(PLACEHOLDER)
            }
        }

    @Test
    fun `should hide the placeholder when the field has text`() =
        componentTest {
            GIVEN("the field contains Alex") {
                useState("Alex")
            }

            WHEN("it is shown") {
                show()
            }

            THEN("the placeholder is hidden") {
                textFieldRobot.assertPlaceholderNotShown(PLACEHOLDER)
            }
        }

    @Test
    fun `should report the typed text when the player types`() =
        componentTest {
            GIVEN("an empty field is showing") {
                useState("")
                show()
            }

            WHEN("the player types Alex") {
                textFieldRobot.enterText(LABEL, "Alex")
            }

            THEN("Alex is reported") {
                actions.assertEmitted("Alex")
            }
        }

    private companion object {
        const val LABEL = "Your name"
        const val PLACEHOLDER = "What should we call you?"
    }
}
