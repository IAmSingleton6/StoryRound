package com.example.storyround.presentation.home

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.storyround.ui.theme.StoryRoundTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class HomeScreenTest {
    @Test
    fun `given saved stories when the history pill is pressed then its count is shown and history is opened`() =
        runComposeUiTest {
            var action: HomeAction? = null
            setContent {
                StoryRoundTheme {
                    HomeScreen(HomeUiState(historyCount = 3)) { action = it }
                }
            }

            onNodeWithContentDescription("Story history, 3 saved stories").assertIsDisplayed().performClick()

            assertEquals(HomeAction.OpenHistory, action)
        }

    @Test
    fun `given no name when home is shown then create is disabled and join mode can still be opened`() =
        runComposeUiTest {
            var action: HomeAction? = null
            setContent {
                StoryRoundTheme {
                    HomeScreen(HomeUiState()) { action = it }
                }
            }

            onNodeWithText("CREATE GAME").assertIsNotEnabled()
            onNodeWithText("JOIN GAME").assertIsEnabled().performClick()

            assertEquals(HomeAction.ShowJoinGame, action)
        }

    @Test
    fun `given a name shorter than the minimum when home is shown then create is disabled`() =
        runComposeUiTest {
            setContent { StoryRoundTheme { HomeScreen(HomeUiState(playerName = " A ")) {} } }

            onNodeWithText("CREATE GAME").assertIsNotEnabled()
        }

    @Test
    fun `given join mode with a name and room code when join and cancel are pressed then both actions are emitted`() =
        runComposeUiTest {
            val actions = mutableListOf<HomeAction>()
            setContent {
                StoryRoundTheme {
                    HomeScreen(HomeUiState(playerName = "Sam", hasValidPlayerName = true, roomCode = "7F2K", isJoining = true)) {
                        actions +=
                            it
                    }
                }
            }

            onNodeWithText("JOIN GAME").assertIsEnabled().performClick()
            onNodeWithText("Cancel").performClick()

            assertEquals(listOf(HomeAction.JoinGame, HomeAction.CancelJoin), actions)
        }

    @Test
    fun `given join mode without a room code when home is shown then join is disabled`() =
        runComposeUiTest {
            setContent {
                StoryRoundTheme {
                    HomeScreen(HomeUiState(playerName = "Sam", hasValidPlayerName = true, isJoining = true)) {}
                }
            }

            onNodeWithText("ABCD").assertIsDisplayed()
            onNodeWithText("JOIN GAME").assertIsNotEnabled()
        }

    @Test
    fun `given a connection in progress when home is shown then the primary action reports connecting`() =
        runComposeUiTest {
            setContent {
                StoryRoundTheme {
                    HomeScreen(HomeUiState(playerName = "Alex", hasValidPlayerName = true, isConnecting = true)) {}
                }
            }

            onNodeWithText("CONNECTING…").assertIsNotEnabled()
        }

    @Test
    fun `given a server error when home is shown then the error banner is displayed`() =
        runComposeUiTest {
            setContent {
                StoryRoundTheme {
                    HomeScreen(HomeUiState(error = "We couldn't find that room.")) {}
                }
            }

            onNodeWithText("We couldn't find that room.").assertIsDisplayed()
        }
}
