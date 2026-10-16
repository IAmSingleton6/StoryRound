package com.example.storyround.presentation.home

import com.example.storyround.presentation.model.UiText
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.ComponentTest
import kotlin.test.Test

internal class HomeScreenTest : ComponentTest<HomeUiState, HomeAction>({ state, onAction -> HomeScreen(state, onAction) }) {
    @Test
    fun `should announce how many stories are saved when home is shown`() =
        componentTest {
            GIVEN("three stories are saved on this device") {
                useState(HomeUiState(historyCount = 3))
            }

            WHEN("Home is shown") {
                show()
            }

            THEN("the history pill announces three saved stories") {
                homeRobot.assertHistoryCount("3 saved stories")
            }
        }

    @Test
    fun `should announce no saved stories when nothing has been saved`() =
        componentTest {
            GIVEN("no stories are saved on this device") {
                useState(HomeUiState(historyCount = 0))
            }

            WHEN("Home is shown") {
                show()
            }

            THEN("the history pill announces no saved stories") {
                homeRobot.assertHistoryCount("0 saved stories")
            }
        }

    @Test
    fun `should open history when the history pill is pressed`() =
        componentTest {
            GIVEN("Home is showing three saved stories") {
                useState(HomeUiState(historyCount = 3))
                show()
            }

            WHEN("the player presses the history pill") {
                homeRobot.tapHistory()
            }

            THEN("the open history action is emitted") {
                actions.assertEmitted(HomeAction.OpenHistory)
            }
        }

    @Test
    fun `should emit the typed name when a name is typed`() =
        componentTest {
            GIVEN("Home is showing with no name") {
                useState(HomeUiState())
                show()
            }

            WHEN("the player types Alex") {
                homeRobot.enterName("Alex")
            }

            THEN("the name change is emitted") {
                actions.assertEmitted(HomeAction.PlayerNameChanged("Alex"))
            }
        }

    @Test
    fun `should emit create game when create is pressed with a valid name`() =
        componentTest {
            GIVEN("Home is showing a valid name") {
                useState(HomeUiState(playerName = "Alex", hasValidPlayerName = true))
                show()
            }

            WHEN("the player presses create") {
                homeRobot.tapCreateGame()
            }

            THEN("the create game action is emitted") {
                actions.assertEmitted(HomeAction.CreateGame)
            }
        }

    @Test
    fun `should disable create when no name has been entered`() =
        componentTest {
            GIVEN("no name has been entered") {
                useState(HomeUiState())
            }

            WHEN("Home is shown") {
                show()
            }

            THEN("create is disabled") {
                homeRobot.assertCreateEnabled(false)
            }
        }

    @Test
    fun `should disable create when the name is shorter than the minimum`() =
        componentTest {
            GIVEN("the name is a single letter between spaces") {
                useState(HomeUiState(playerName = " A "))
            }

            WHEN("Home is shown") {
                show()
            }

            THEN("create is disabled") {
                homeRobot.assertCreateEnabled(false)
            }
        }

    @Test
    fun `should emit show join game when join is pressed without a name`() =
        componentTest {
            GIVEN("Home is showing with no name") {
                useState(HomeUiState())
                show()
            }

            WHEN("the player presses join") {
                homeRobot.tapJoinGame()
            }

            THEN("the show join game action is emitted") {
                actions.assertEmitted(HomeAction.ShowJoinGame)
            }
        }

    @Test
    fun `should show the room code field instead of create when join mode is open`() =
        componentTest {
            GIVEN("join mode is open") {
                useState(HomeUiState(playerName = "Sam", hasValidPlayerName = true, isJoining = true))
            }

            WHEN("Home is shown") {
                show()
            }

            THEN("the room code field replaces create") {
                homeRobot.assertRoomCodeFieldShown()
                homeRobot.assertCreateNotShown()
            }
        }

    @Test
    fun `should emit the typed code when a room code is typed`() =
        componentTest {
            GIVEN("Home is in join mode") {
                useState(HomeUiState(playerName = "Sam", hasValidPlayerName = true, isJoining = true))
                show()
            }

            WHEN("the player types a room code") {
                homeRobot.enterRoomCode("7f2k")
            }

            THEN("the code change is emitted as typed") {
                actions.assertEmitted(HomeAction.RoomCodeChanged("7f2k"))
            }
        }

    @Test
    fun `should emit join game when join is pressed with a name and room code`() =
        componentTest {
            GIVEN("Home is in join mode with a name and room code") {
                useState(HomeUiState(playerName = "Sam", hasValidPlayerName = true, roomCode = "7F2K", isJoining = true))
                show()
            }

            WHEN("the player presses join") {
                homeRobot.tapJoinGame()
            }

            THEN("the join game action is emitted") {
                actions.assertEmitted(HomeAction.JoinGame)
            }
        }

    @Test
    fun `should emit cancel join when cancel is pressed`() =
        componentTest {
            GIVEN("Home is in join mode") {
                useState(HomeUiState(playerName = "Sam", hasValidPlayerName = true, roomCode = "7F2K", isJoining = true))
                show()
            }

            WHEN("the player presses cancel") {
                homeRobot.tapCancel()
            }

            THEN("the cancel join action is emitted") {
                actions.assertEmitted(HomeAction.CancelJoin)
            }
        }

    @Test
    fun `should disable join when no room code has been entered`() =
        componentTest {
            GIVEN("join mode is open with a name but no room code") {
                useState(HomeUiState(playerName = "Sam", hasValidPlayerName = true, isJoining = true))
            }

            WHEN("Home is shown") {
                show()
            }

            THEN("join is disabled and the code placeholder is shown") {
                homeRobot.assertJoinEnabled(false)
                homeRobot.assertRoomCodePlaceholderShown()
            }
        }

    @Test
    fun `should report connecting on create when a connection is in progress`() =
        componentTest {
            GIVEN("the app is connecting") {
                useState(HomeUiState(playerName = "Alex", hasValidPlayerName = true, isConnecting = true))
            }

            WHEN("Home is shown") {
                show()
            }

            THEN("the primary action reads connecting and is disabled") {
                homeRobot.assertConnecting()
            }
        }

    @Test
    fun `should report connecting on join when a connection is in progress in join mode`() =
        componentTest {
            GIVEN("the app is connecting while join mode is open") {
                useState(
                    HomeUiState(playerName = "Sam", hasValidPlayerName = true, roomCode = "7F2K", isJoining = true, isConnecting = true),
                )
            }

            WHEN("Home is shown") {
                show()
            }

            THEN("the join action reads connecting and is disabled") {
                homeRobot.assertConnecting()
            }
        }

    @Test
    fun `should announce the error when the server reports one`() =
        componentTest {
            GIVEN("the server could not find the room") {
                useState(HomeUiState(error = UiText.Plain("We couldn't find that room.")))
            }

            WHEN("Home is shown") {
                show()
            }

            THEN("the error banner announces it") {
                homeRobot.assertErrorShown("We couldn't find that room.")
            }
        }
}
