package com.example.storyround.testing.robot.component

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import com.example.storyround.testing.robot.Robot

class TextFieldRobot(
    ui: ComposeUiTest,
) : Robot(ui) {
    fun enterText(
        label: String,
        text: String,
    ) = typeInto(label, text)

    fun assertLabelShown(label: String) = assertTextShown(label)

    fun assertLabelled(label: String) {
        await(hasSetTextAction() and hasContentDescription(label))
    }

    fun assertPlaceholderShown(placeholder: String) = assertTextShown(placeholder)

    fun assertPlaceholderNotShown(placeholder: String) = assertTextNotShown(placeholder)
}
