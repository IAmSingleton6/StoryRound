package com.example.storyround.presentation.history

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.ShelfStoryUi
import com.example.storyround.presentation.model.StoryWriterUi
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.ui.theme.StoryRoundTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class HistoryScreenTest {
    private val alex = PlayerUi("alex", "Alex", PlayerColour.Blue)
    private val sam = PlayerUi("sam", "Sam", PlayerColour.Yellow)

    @Test
    fun `given saved stories when a card and back are pressed then the story opens and the shelf closes`() =
        runComposeUiTest {
            val actions = mutableListOf<HistoryAction>()
            val writers = listOf(StoryWriterUi(alex, 2), StoryWriterUi(sam, 2))
            val story = ShelfStoryUi("story-1", "A door opened.", listOf(alex, sam), 3, writers)
            setContent { StoryRoundTheme { HistoryScreen(HistoryUiState.Content(listOf(story))) { actions += it } } }

            onNodeWithText("Your shelf").assertIsDisplayed()
            onNodeWithText("2 players · 3 contributions").assertIsDisplayed()
            onNodeWithText("A door opened.").performClick()
            onNodeWithContentDescription("Back").performClick()

            assertEquals(listOf(HistoryAction.OpenStory("story-1"), HistoryAction.Back), actions)
        }

    @Test
    fun `given no saved stories when back is pressed then the empty shelf closes`() =
        runComposeUiTest {
            var action: HistoryAction? = null
            setContent { StoryRoundTheme { HistoryScreen(HistoryUiState.Content(emptyList())) { action = it } } }

            onNodeWithText("No completed stories yet.").assertIsDisplayed()
            onNodeWithText("Finish a game and the story is saved here, on this device.").assertIsDisplayed()
            onNodeWithText("BACK").performClick()

            assertEquals(HistoryAction.Back, action)
        }
}
