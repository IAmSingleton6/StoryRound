package com.example.storyround.testing.robot.component

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import com.example.storyround.testing.robot.Robot

class ButtonRobot(
    ui: ComposeUiTest,
) : Robot(ui) {
    fun tapButton(label: String) = tap(label)

    fun tapDisabledButton(label: String) {
        await(hasText(label)).performClick()
    }

    fun assertButtonEnabled(
        label: String,
        enabled: Boolean,
    ) = assertControlEnabled(label, enabled)
}
