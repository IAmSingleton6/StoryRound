package com.example.storyround.testing.robot.component

import androidx.compose.ui.test.ComposeUiTest
import com.example.storyround.testing.robot.Robot

class StoryTextRobot(
    ui: ComposeUiTest,
) : Robot(ui) {
    fun assertStoryShown(text: String) = assertDescriptionShown(text)

    fun assertNewestTag(tag: String) = assertTextShown(tag)

    fun assertNoNewestTag(tag: String) = assertTextNotShown(tag)
}
