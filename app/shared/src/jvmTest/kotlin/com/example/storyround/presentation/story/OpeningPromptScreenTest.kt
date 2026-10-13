package com.example.storyround.presentation.story

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.RoomUi
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.ui.theme.StoryRoundTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class OpeningPromptScreenTest {
    private val alex = PlayerUi("alex", "Alex", PlayerColour.Blue)
    private val sam = PlayerUi("sam", "Sam", PlayerColour.Yellow)
    private val room =
        RoomUi(
            roomCode = "AB12",
            hostId = alex.id,
            players = listOf(alex, sam),
            maxPlayers = 10,
            prompt = "",
            contributions = emptyList(),
            turn = null,
            maxRounds = 2,
        )

    @Test
    fun `given the opening writer with a blank draft when shown then there is no timer and start is disabled`() =
        runComposeUiTest {
            setContent { StoryRoundTheme { StoryScreen(StoryUiState.OpeningPrompt(room, "", alex, isOpeningWriter = true)) {} } }

            onNodeWithText("NO TIMER").assertIsDisplayed()
            onNodeWithText("Write the opening line or premise. Take your time.").assertIsDisplayed()
            onNodeWithText("0/280").assertIsDisplayed()
            onNodeWithText("START STORY").assertIsNotEnabled()
        }

    @Test
    fun `given the opening writer with a draft when start is pressed then the prompt is submitted`() =
        runComposeUiTest {
            var action: StoryAction? = null
            setContent {
                StoryRoundTheme {
                    StoryScreen(StoryUiState.OpeningPrompt(room, "A door opened.", alex, isOpeningWriter = true)) { action = it }
                }
            }

            onNodeWithText("14/280").assertIsDisplayed()
            onNodeWithText("START STORY").assertIsEnabled().performClick()

            assertEquals(StoryAction.SubmitOpeningPrompt, action)
        }

    @Test
    fun `given the opening writer when they type then each change is emitted`() =
        runComposeUiTest {
            val actions = mutableListOf<StoryAction>()
            setContent {
                var uiState by remember { mutableStateOf(StoryUiState.OpeningPrompt(room, "", alex, isOpeningWriter = true)) }
                StoryRoundTheme {
                    StoryScreen(uiState) { action ->
                        actions += action
                        if (action is StoryAction.OpeningPromptChanged) uiState = uiState.copy(openingPromptDraft = action.value)
                    }
                }
            }

            onNode(hasSetTextAction()).performTextInput("A door opened.")
            onNodeWithText("START STORY").performClick()

            assertEquals(listOf(StoryAction.OpeningPromptChanged("A door opened."), StoryAction.SubmitOpeningPrompt), actions)
        }

    @Test
    fun `given another player opens when shown then the writing field is replaced by a waiting pill`() =
        runComposeUiTest {
            setContent { StoryRoundTheme { StoryScreen(StoryUiState.OpeningPrompt(room, "", alex, isOpeningWriter = false)) {} } }

            onNodeWithText("Alex is starting the story…").assertIsDisplayed()
            onNodeWithText("Waiting for Alex…").assertIsDisplayed()
            onNode(hasSetTextAction()).assertDoesNotExist()
            onNodeWithText("START STORY").assertDoesNotExist()
        }

    @Test
    fun `given a dropped connection when the opening is shown then the reconnecting banner is displayed`() =
        runComposeUiTest {
            setContent {
                StoryRoundTheme {
                    StoryScreen(StoryUiState.OpeningPrompt(room, "", alex, isOpeningWriter = false, isReconnecting = true)) {}
                }
            }

            onNodeWithText("Reconnecting… your seat is saved").assertIsDisplayed()
        }
}
