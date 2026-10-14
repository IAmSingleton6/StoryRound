package com.example.storyround.presentation.history

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.storyround.presentation.model.ContributionUi
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.StoryUi
import com.example.storyround.presentation.model.StoryWriterUi
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.ui.theme.StoryRoundTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class HistoryScreenTest {
    private val alex = PlayerUi("alex", "Alex", PlayerColour.Blue)
    private val sam = PlayerUi("sam", "Sam", PlayerColour.Yellow)
    private val story =
        StoryUi(
            id = "story-1",
            prompt = "A door opened.",
            players = listOf(alex, sam),
            contributions = List(3) { turn -> ContributionUi(if (turn % 2 == 0) sam else alex, "Piece $turn.", turn) },
            promptAuthor = alex,
            writers = listOf(StoryWriterUi(alex, 2), StoryWriterUi(sam, 2)),
        )

    @Test
    fun `given saved stories when a card and back are pressed then the story opens and history closes`() =
        runComposeUiTest {
            val actions = mutableListOf<HistoryAction>()
            setContent { StoryRoundTheme { HistoryScreen(HistoryUiState.Content(listOf(story))) { actions += it } } }

            onNodeWithText("Story history").assertIsDisplayed()
            onNodeWithText("2 players · 3 contributions").assertIsDisplayed()
            onNodeWithText("A door opened.").performClick()
            onNodeWithContentDescription("Back").performClick()

            assertEquals(listOf(HistoryAction.OpenStory("story-1"), HistoryAction.Back), actions)
        }

    @Test
    fun `given no saved stories when back is pressed then the empty history closes`() =
        runComposeUiTest {
            var action: HistoryAction? = null
            setContent { StoryRoundTheme { HistoryScreen(HistoryUiState.Content(emptyList())) { action = it } } }

            onNodeWithText("No completed stories yet.").assertIsDisplayed()
            onNodeWithText("Finish a game and the story is saved here, on this device.").assertIsDisplayed()
            onNodeWithText("BACK").performClick()

            assertEquals(HistoryAction.Back, action)
        }

    @Test
    fun `given a connection error when history is shown then the error is announced above the saved stories`() =
        runComposeUiTest {
            setContent { StoryRoundTheme { HistoryScreen(HistoryUiState.Content(listOf(story), error = "Reconnecting...")) {} } }

            onNodeWithText("Reconnecting...").assertIsDisplayed()
            onNodeWithText("A door opened.").assertIsDisplayed()
        }
}
