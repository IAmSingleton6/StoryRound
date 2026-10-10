package com.example.storyround.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.storyround.presentation.finished.FinishedAction
import com.example.storyround.presentation.finished.FinishedScreen
import com.example.storyround.presentation.finished.FinishedUiState
import com.example.storyround.presentation.history.HistoryAction
import com.example.storyround.presentation.history.HistoryScreen
import com.example.storyround.presentation.history.HistoryUiState
import com.example.storyround.presentation.home.HomeAction
import com.example.storyround.presentation.home.HomeScreen
import com.example.storyround.presentation.home.HomeUiState
import com.example.storyround.presentation.lobby.LobbyAction
import com.example.storyround.presentation.lobby.LobbyScreen
import com.example.storyround.presentation.lobby.LobbyUiState
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.RoomUi
import com.example.storyround.presentation.model.StoryUi
import com.example.storyround.presentation.model.TurnUi
import com.example.storyround.presentation.story.StoryAction
import com.example.storyround.presentation.story.StoryScreen
import com.example.storyround.presentation.story.StoryUiState
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.StorySummary
import com.example.storyround.ui.theme.StoryRoundTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ScreenSemanticsTest {
    @Test
    fun `given a valid home form when create is pressed then submit action is emitted`() =
        runComposeUiTest {
            var action: HomeAction? = null
            setContent {
                StoryRoundTheme {
                    HomeScreen(HomeUiState(playerName = "Alex")) { action = it }
                }
            }

            onNodeWithText("CREATE GAME").assertIsDisplayed().performClick()

            assertEquals(HomeAction.CreateGame, action)
        }

    @Test
    fun `given another players turn when story is displayed then contribution input is disabled`() =
        runComposeUiTest {
            val alex = PlayerUi("alex", "Alex", PlayerColour.Blue)
            val sam = PlayerUi("sam", "Sam", PlayerColour.Green)
            setContent {
                StoryRoundTheme {
                    StoryScreen(
                        uiState =
                            StoryUiState.Playing(
                                room =
                                    RoomUi(
                                        roomCode = "AB12",
                                        hostId = alex.id,
                                        players = listOf(alex, sam),
                                        prompt = "A door opened.",
                                        contributions = emptyList(),
                                        turn = TurnUi(sam, number = 1, deadlineEpochMillis = Long.MAX_VALUE),
                                        maxRounds = 3,
                                    ),
                                playerId = alex.id,
                                nowEpochMillis = 0,
                                contributionDraft = "",
                            ),
                        onAction = {},
                    )
                }
            }

            onNodeWithText("Waiting for Sam...").assertIsDisplayed()
            onNodeWithText("SUBMIT").assertIsNotEnabled()
        }

    @Test
    fun `given a host lobby when start is pressed then players and start action are exposed`() =
        runComposeUiTest {
            val alex = PlayerUi("alex", "Alex", PlayerColour.Blue)
            var action: LobbyAction? = null
            setContent {
                StoryRoundTheme {
                    LobbyScreen(
                        LobbyUiState("AB12", listOf(alex), maxPlayers = 10, hostId = alex.id, currentPlayerId = alex.id, maxRounds = 3),
                    ) { action = it }
                }
            }

            onNodeWithText("AB12").assertIsDisplayed()
            onNodeWithText("1 / 10 PLAYERS").assertIsDisplayed()
            onNodeWithText("START GAME").performClick()

            assertEquals(LobbyAction.StartGame, action)
        }

    @Test
    fun `given the opening prompt owner when text is entered and started then story actions are emitted`() =
        runComposeUiTest {
            val alex = PlayerUi("alex", "Alex", PlayerColour.Blue)
            val actions = mutableListOf<StoryAction>()
            setContent {
                var uiState by
                    remember {
                        mutableStateOf(
                            StoryUiState.OpeningPrompt(
                                room = RoomUi("AB12", alex.id, listOf(alex), "", emptyList(), null, maxRounds = 3),
                                openingPromptDraft = "",
                                openingPlayer = alex,
                                isOpeningWriter = true,
                            ),
                        )
                    }
                StoryRoundTheme {
                    StoryScreen(uiState) { action ->
                        actions += action
                        if (action is StoryAction.OpeningPromptChanged) {
                            uiState = uiState.copy(openingPromptDraft = action.value)
                        }
                    }
                }
            }

            onNode(hasSetTextAction()).performTextInput("A door opened.")
            onNodeWithText("START STORY").performClick()

            assertEquals(
                listOf(StoryAction.OpeningPromptChanged("A door opened."), StoryAction.SubmitOpeningPrompt),
                actions,
            )
        }

    @Test
    fun `given the active player when story text is entered then submit is enabled and emitted`() =
        runComposeUiTest {
            val alex = PlayerUi("alex", "Alex", PlayerColour.Blue)
            var action: StoryAction? = null
            setContent {
                var uiState by
                    remember {
                        mutableStateOf(
                            StoryUiState.Playing(
                                room =
                                    RoomUi(
                                        "AB12",
                                        alex.id,
                                        listOf(alex),
                                        "A door opened.",
                                        emptyList(),
                                        TurnUi(alex, number = 1, deadlineEpochMillis = Long.MAX_VALUE),
                                        maxRounds = 3,
                                    ),
                                playerId = alex.id,
                                nowEpochMillis = 0,
                                contributionDraft = "",
                            ),
                        )
                    }
                StoryRoundTheme {
                    StoryScreen(uiState) { storyAction ->
                        action = storyAction
                        if (storyAction is StoryAction.ContributionChanged) {
                            uiState = uiState.copy(contributionDraft = storyAction.value)
                        }
                    }
                }
            }

            onNodeWithText("Continue the story...").performTextInput("A horse arrived.")
            onNodeWithText("SUBMIT").performClick()

            assertEquals(StoryAction.SubmitContribution, action)
        }

    @Test
    fun `given finished content when view story is selected then action is exposed`() =
        runComposeUiTest {
            val alex = PlayerUi("alex", "Alex", PlayerColour.Blue)
            var finishedAction: FinishedAction? = null
            setContent {
                StoryRoundTheme {
                    FinishedScreen(
                        FinishedUiState(
                            story = StoryUi("AB12", "A door opened.", listOf(alex), emptyList()),
                            canViewStory = true,
                            canPlayAgain = true,
                        ),
                    ) { finishedAction = it }
                }
            }

            onNodeWithText("VIEW STORY").performClick()

            assertEquals(FinishedAction.ViewStory, finishedAction)
        }

    @Test
    fun `given history content when a saved story is selected then open action is exposed`() =
        runComposeUiTest {
            var historyAction: HistoryAction? = null
            setContent {
                StoryRoundTheme {
                    HistoryScreen(
                        HistoryUiState.Content(listOf(StorySummary("story-1", "Saved prompt", 1, 2, 3))),
                    ) { historyAction = it }
                }
            }

            onNodeWithText("Saved prompt").performClick()

            assertEquals(HistoryAction.OpenStory("story-1"), historyAction)
        }
}
