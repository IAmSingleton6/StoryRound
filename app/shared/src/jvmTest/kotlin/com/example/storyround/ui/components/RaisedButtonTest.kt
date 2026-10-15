package com.example.storyround.ui.components

import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.ComponentTest
import kotlin.test.Test

class RaisedButtonTest :
    ComponentTest<Boolean, RaisedButtonTest.Press>({ enabled, onPress ->
        RaisedButton(label = "START GAME", onClick = { onPress(RaisedButtonTest.Press) }, enabled = enabled)
    }) {
    @Test
    fun `should report one press when an enabled button is pressed`() =
        componentTest {
            GIVEN("an enabled START GAME button is showing") {
                useState(true)
                show()
            }

            WHEN("the player presses it") {
                buttonRobot.tapButton("START GAME")
            }

            THEN("one press is reported") {
                actions.assertEmitted(Press)
            }
        }

    @Test
    fun `should ignore presses when the button is disabled`() =
        componentTest {
            GIVEN("a disabled START GAME button is showing") {
                useState(false)
                show()
            }

            WHEN("the player presses it") {
                buttonRobot.tapDisabledButton("START GAME")
            }

            THEN("it is shown as disabled and nothing is reported") {
                buttonRobot.assertButtonEnabled("START GAME", enabled = false)
                actions.assertNothingEmitted()
            }
        }

    data object Press
}
