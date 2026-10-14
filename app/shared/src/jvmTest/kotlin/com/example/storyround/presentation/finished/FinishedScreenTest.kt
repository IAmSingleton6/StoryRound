package com.example.storyround.presentation.finished

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
class FinishedScreenTest {
    private val alex = PlayerUi("alex", "Alex", PlayerColour.Blue)
    private val sam = PlayerUi("sam", "Sam", PlayerColour.Yellow)
    private val finished =
        FinishedUiState(
            segments = listOf(StorySegmentUi("A door opened.", alex), StorySegmentUi("A horse arrived.", sam)),
            writers = listOf(StoryWriterUi(alex, 1), StoryWriterUi(sam, 1)),
            contributionCount = 1,
            host = alex,
            playerId = alex.id,
            canPlayAgain = true,
        )

    @Test
    fun `given a host on a finished story when the actions are pressed then each is emitted`() =
        runComposeUiTest {
            val actions = mutableListOf<FinishedAction>()
            setContent { StoryRoundTheme { FinishedScreen(finished) { actions += it } } }

            onNodeWithText("STORY COMPLETE").assertIsDisplayed()
            onNodeWithText("1 contribution · 2 writers · saved to your history").assertIsDisplayed()
            onNodeWithContentDescription("Alex (you), 1 pieces").assertIsDisplayed()
            onNodeWithText("PLAY AGAIN").performClick()
            onNodeWithText("Home").performClick()
            onNodeWithText("View story").assertDoesNotExist()
            onNodeWithText("Story history").assertDoesNotExist()

            assertEquals(listOf(FinishedAction.PlayAgain, FinishedAction.Home), actions)
        }

    @Test
    fun `given a guest when shown then the host is named and only home is offered`() =
        runComposeUiTest {
            setContent {
                StoryRoundTheme {
                    FinishedScreen(finished.copy(playerId = sam.id, canPlayAgain = false)) {}
                }
            }

            onNodeWithText("Alex can start another round from here.").assertIsDisplayed()
            onNodeWithText("PLAY AGAIN").assertDoesNotExist()
            onNodeWithText("Home").assertIsDisplayed()
        }

    @Test
    fun `given a dropped connection when the finished story is shown then the reconnecting banner is displayed`() =
        runComposeUiTest {
            setContent { StoryRoundTheme { FinishedScreen(finished.copy(isReconnecting = true)) {} } }

            onNodeWithText("Reconnecting… your seat is saved").assertIsDisplayed()
        }
}
