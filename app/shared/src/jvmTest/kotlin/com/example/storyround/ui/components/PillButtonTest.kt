package com.example.storyround.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.unit.dp
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.ComponentTest
import kotlin.test.Test

class PillButtonTest :
    ComponentTest<String, PillButtonTest.Press>({ label, onPress ->
        PillButton(onClick = { onPress(PillButtonTest.Press) }, contentPadding = PaddingValues(horizontal = 16.dp)) {
            BasicText(label)
        }
    }) {
    @Test
    fun `should report one press when the pill is pressed`() =
        componentTest {
            GIVEN("a Leave lobby pill is showing") {
                useState("Leave lobby")
                show()
            }

            WHEN("the player presses it") {
                buttonRobot.tapButton("Leave lobby")
            }

            THEN("one press is reported") {
                actions.assertEmitted(Press)
            }
        }

    data object Press
}
