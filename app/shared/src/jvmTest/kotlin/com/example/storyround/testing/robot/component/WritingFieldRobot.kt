package com.example.storyround.testing.robot.component

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import com.example.storyround.testing.robot.Robot

class WritingFieldRobot(
    ui: ComposeUiTest,
) : Robot(ui) {
    fun enterText(text: String) {
        await(hasSetTextAction()).performTextInput(text)
    }

    fun tapIdea() = tap("Idea")

    fun tapSend(label: String) = tap(label)

    fun pressKeyboardSend() {
        await(hasSetTextAction()).performImeAction()
    }

    fun assertCounter(counter: String) = assertTextShown(counter)

    fun assertError(message: String) = assertTextShown(message)

    fun assertNoIdea() = awaitGone(hasText("Idea"))

    fun assertSendEnabled(
        label: String,
        enabled: Boolean,
    ) = assertControlEnabled(label, enabled)

    fun assertShown() {
        await(hasSetTextAction())
    }

    fun assertNotShown() = awaitGone(hasSetTextAction())

    fun assertFocused() {
        await(hasSetTextAction()).assertIsFocused()
    }

    fun assertLabelled(placeholder: String) {
        await(hasSetTextAction() and hasContentDescription(placeholder))
    }
}
