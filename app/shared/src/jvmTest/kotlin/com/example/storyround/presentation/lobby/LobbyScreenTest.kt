package com.example.storyround.presentation.lobby

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.ui.theme.StoryRoundTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class LobbyScreenTest {
    private val alex = PlayerUi("alex", "Alex", PlayerColour.Blue)
    private val sam = PlayerUi("sam", "Sam", PlayerColour.Yellow)
    private val hostLobby =
        LobbyUiState(
            roomCode = "7F2K",
            players = listOf(alex, sam),
            maxPlayers = 10,
            hostId = alex.id,
            currentPlayerId = alex.id,
            maxRounds = 1,
            canDecreaseRounds = false,
            canIncreaseRounds = true,
        )

    @Test
    fun `given a host lobby when shown then the ticket, seats and host settings are displayed`() =
        runComposeUiTest {
            setContent { StoryRoundTheme { LobbyScreen(hostLobby) {} } }

            onNodeWithText("7F2K").assertIsDisplayed()
            onNodeWithText("2 / 10 PLAYERS").assertIsDisplayed()
            onNodeWithText("8 seats open").assertIsDisplayed()
            onNodeWithText("Random player").assertIsDisplayed()
            onNodeWithText("Random").assertIsSelected()
            onNodeWithText("Everyone writes 1 time · 2 turns").assertIsDisplayed()
            onNodeWithContentDescription("Fewer rounds").assertIsNotEnabled()
            onNodeWithText("You are the host. Start when you're ready.").assertIsDisplayed()
        }

    @Test
    fun `given a host lobby when an opener, more rounds, start and leave are pressed then the actions are emitted`() =
        runComposeUiTest {
            val actions = mutableListOf<LobbyAction>()
            setContent { StoryRoundTheme { LobbyScreen(hostLobby) { actions += it } } }

            onNode(hasText("Sam") and hasClickAction()).performClick()
            onNodeWithContentDescription("More rounds").performClick()
            onNodeWithText("START GAME").performClick()
            onNodeWithText("Leave lobby").performClick()

            assertEquals(
                listOf(
                    LobbyAction.OpeningPlayerSelected(sam.id),
                    LobbyAction.RoundsChanged(2),
                    LobbyAction.StartGame,
                    LobbyAction.Leave,
                ),
                actions,
            )
        }

    @Test
    fun `given a guest lobby when shown then the host is awaited and no host controls are offered`() =
        runComposeUiTest {
            setContent { StoryRoundTheme { LobbyScreen(hostLobby.copy(currentPlayerId = sam.id)) {} } }

            onNodeWithText("Waiting for Alex to start the game…").assertIsDisplayed()
            onNodeWithText("YOU").assertIsDisplayed()
            onNodeWithText("START GAME").assertDoesNotExist()
            onNodeWithText("Who opens the story?").assertDoesNotExist()
        }

    @Test
    fun `given a dropped connection when the lobby is shown then the reconnecting banner is displayed`() =
        runComposeUiTest {
            setContent { StoryRoundTheme { LobbyScreen(hostLobby.copy(isReconnecting = true)) {} } }

            onNodeWithText("Reconnecting… your seat is saved").assertIsDisplayed()
        }
}
