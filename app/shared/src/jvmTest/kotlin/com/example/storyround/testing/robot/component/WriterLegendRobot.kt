package com.example.storyround.testing.robot.component

import androidx.compose.ui.test.ComposeUiTest
import com.example.storyround.testing.robot.Robot

class WriterLegendRobot(
    ui: ComposeUiTest,
) : Robot(ui) {
    fun assertWriterShown(
        name: String,
        pieces: Int,
    ) = assertDescriptionShown("$name, $pieces ${if (pieces == 1) "piece" else "pieces"}")
}
