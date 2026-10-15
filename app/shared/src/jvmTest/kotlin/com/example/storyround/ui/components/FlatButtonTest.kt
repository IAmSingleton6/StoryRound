package com.example.storyround.ui.components

import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.ComponentTest
import kotlin.test.Test

class FlatButtonTest :
    ComponentTest<Boolean, FlatButtonTest.Press>({ enabled, onPress ->
        FlatButton("Home", onClick = { onPress(FlatButtonTest.Press) }, enabled = enabled)
    }) {
    @Test
    fun `should report one press when an enabled button is pressed`() =
        componentTest {
            GIVEN("an enabled Home button is showing") {
                useState(true)
                show()
            }

            WHEN("the player presses it") {
                buttonRobot.tapButton("Home")
            }

            THEN("one press is reported") {
                actions.assertEmitted(Press)
            }
        }

    @Test
    fun `should ignore presses when the button is disabled`() =
        componentTest {
            GIVEN("a disabled Home button is showing") {
                useState(false)
                show()
            }

            WHEN("the player presses it") {
                buttonRobot.tapDisabledButton("Home")
            }

            THEN("it is shown as disabled and nothing is reported") {
                buttonRobot.assertButtonEnabled("Home", enabled = false)
                actions.assertNothingEmitted()
            }
        }

    data object Press
}
