package com.example.storyround.presentation.history

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.StorySegmentUi
import com.example.storyround.presentation.model.StoryWriterUi
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.ui.theme.StoryRoundTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SavedStoryScreenTest {
    private val alex = PlayerUi("alex", "Alex", PlayerColour.Blue)
    private val sam = PlayerUi("sam", "Sam", PlayerColour.Yellow)
    private val saved =
        SavedStoryUiState(
            segments = listOf(StorySegmentUi("A door opened.", alex), StorySegmentUi("A horse arrived.", sam)),
            writers = listOf(StoryWriterUi(alex, 1), StoryWriterUi(sam, 1)),
        )

    @Test
    fun `given a saved story when shown then the story and its writers are displayed`() =
        runComposeUiTest {
            setContent { StoryRoundTheme { SavedStoryScreen(saved) {} } }

            onNodeWithText("SAVED STORY").assertIsDisplayed()
            onNodeWithContentDescription("A door opened. A horse arrived.").assertIsDisplayed()
            onNodeWithContentDescription("Sam, 1 pieces").assertIsDisplayed()
        }

    @Test
    fun `given a saved story when either back control is pressed then story history is reopened`() =
        runComposeUiTest {
            var backPresses = 0
            setContent { StoryRoundTheme { SavedStoryScreen(saved) { backPresses++ } } }

            onNodeWithContentDescription("Back").performClick()
            onNodeWithText("Back to history").performClick()

            assertEquals(2, backPresses)
        }
}
