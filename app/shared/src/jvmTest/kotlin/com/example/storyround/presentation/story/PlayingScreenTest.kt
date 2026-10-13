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
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.RoomUi
import com.example.storyround.presentation.model.StorySegmentUi
import com.example.storyround.presentation.model.TurnUi
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.ui.theme.StoryRoundTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PlayingScreenTest {
    private val alex = PlayerUi("alex", "Alex", PlayerColour.Blue)
    private val sam = PlayerUi("sam", "Sam", PlayerColour.Yellow)

    private fun playing(
        playerId: String,
        timeLeftMillis: Long,
        draft: String = "",
        nextPlayer: PlayerUi? = sam,
        timeUpHandOver: PlayerUi? = null,
    ) = StoryUiState.Playing(
        room =
            RoomUi(
                roomCode = "AB12",
                hostId = alex.id,
                players = listOf(alex, sam),
                maxPlayers = 10,
                prompt = "A door opened.",
                contributions = emptyList(),
                turn = TurnUi(alex, number = 2, deadlineEpochMillis = 20_000L),
                promptAuthor = alex,
                maxRounds = 2,
            ),
        playerId = playerId,
        nowEpochMillis = 20_000L - timeLeftMillis,
        contributionDraft = draft,
        segments = listOf(StorySegmentUi("A door opened.", alex)),
        round = 2,
        turn = 3,
        totalTurns = 4,
        turnDurationMillis = 13_000L,
        nextPlayer = nextPlayer,
        nextTurnDurationMillis = 12_000L,
        timeUpHandOver = timeUpHandOver,
    )

    @Test
    fun `given your turn with a draft when shown then the turn, countdown and submit are ready`() =
        runComposeUiTest {
            var action: StoryAction? = null
            setContent { StoryRoundTheme { StoryScreen(playing(alex.id, 7_300L, draft = "A horse arrived.")) { action = it } } }

            onNodeWithText("ROUND 2 OF 2").assertIsDisplayed()
            onNodeWithText("Turn 3 of 4").assertIsDisplayed()
            onNodeWithText("YOUR TURN").assertIsDisplayed()
            onNodeWithText("13s turn · next: Sam, 12s").assertIsDisplayed()
            onNodeWithText("7.3").assertIsDisplayed()
            onNodeWithContentDescription("Time left").assertIsDisplayed()
            onNodeWithText("SUBMIT").assertIsEnabled().performClick()

            assertEquals(StoryAction.SubmitContribution, action)
        }

    @Test
    fun `given your turn when text is typed then the change is emitted`() =
        runComposeUiTest {
            val actions = mutableListOf<StoryAction>()
            setContent {
                var uiState by remember { mutableStateOf(playing(alex.id, 7_300L)) }
                StoryRoundTheme {
                    StoryScreen(uiState) { action ->
                        actions += action
                        if (action is StoryAction.ContributionChanged) uiState = uiState.copy(contributionDraft = action.value)
                    }
                }
            }

            onNode(hasSetTextAction()).performTextInput("A horse arrived.")
            onNodeWithText("SUBMIT").assertIsEnabled().performClick()

            assertEquals(listOf(StoryAction.ContributionChanged("A horse arrived."), StoryAction.SubmitContribution), actions)
        }

    @Test
    fun `given your turn with a blank draft on the final turn when shown then submit is disabled`() =
        runComposeUiTest {
            setContent { StoryRoundTheme { StoryScreen(playing(alex.id, 3_000L, nextPlayer = null)) {} } }

            onNodeWithText("13s turn · final turn").assertIsDisplayed()
            onNodeWithText("SUBMIT").assertIsNotEnabled()
        }

    @Test
    fun `given someone else's turn when shown then their writing is awaited and the newest piece is tagged`() =
        runComposeUiTest {
            setContent { StoryRoundTheme { StoryScreen(playing(sam.id, 4_200L)) {} } }

            onNodeWithText("ALEX’S TURN").assertIsDisplayed()
            onNodeWithText("13s turn · next: you, 12s").assertIsDisplayed()
            onNodeWithText("Alex is writing…").assertIsDisplayed()
            onNodeWithContentDescription("A door opened.").assertIsDisplayed()
            onNodeWithText("SUBMIT").assertDoesNotExist()
        }

    @Test
    fun `given an expired turn when shown then the hand-over toast names the next writer`() =
        runComposeUiTest {
            setContent { StoryRoundTheme { StoryScreen(playing(sam.id, 12_000L, timeUpHandOver = alex)) {} } }

            onNodeWithText("Time’s up! Over to Alex.").assertIsDisplayed()
        }
}
