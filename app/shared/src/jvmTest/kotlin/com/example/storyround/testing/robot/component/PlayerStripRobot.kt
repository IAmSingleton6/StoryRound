package com.example.storyround.testing.robot.component

import androidx.compose.ui.test.ComposeUiTest
import com.example.storyround.testing.robot.Robot

class PlayerStripRobot(
    ui: ComposeUiTest,
) : Robot(ui) {
    fun assertPlayerShown(name: String) = assertDescriptionShown(name)

    fun assertWriting(name: String) = assertDescriptionShown("$name, writing now")

    fun assertInitialShown(initial: String) = assertTextShown(initial)
}
