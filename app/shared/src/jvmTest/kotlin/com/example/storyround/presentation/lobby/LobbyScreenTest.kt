package com.example.storyround.presentation.lobby

import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.ComponentTest
import com.example.storyround.testing.fixtures.alex
import com.example.storyround.testing.fixtures.guestLobby
import com.example.storyround.testing.fixtures.hostLobby
import com.example.storyround.testing.fixtures.sam
import kotlin.test.Test

internal class LobbyScreenTest : ComponentTest<LobbyUiState, LobbyAction>({ state, onAction -> LobbyScreen(state, onAction) }) {
    @Test
    fun `should show the room code and seats when the lobby is shown`() =
        componentTest {
            GIVEN("Alex hosts a ten-seat room 7F2K with Sam") {
                useState(hostLobby())
            }

            WHEN("the lobby is shown") {
                show()
            }

            THEN("the ticket shows the code and the seats show who is in and how many are free") {
                lobbyRobot.assertRoomCodeShown("7F2K")
                lobbyRobot.assertPlayerCount("2 / 10 PLAYERS")
                lobbyRobot.assertPlayersShown("Alex", "Sam")
                lobbyRobot.assertSeatsOpen(8)
            }
        }

    @Test
    fun `should hide the open seats when the room is full`() =
        componentTest {
            GIVEN("all ten seats are taken") {
                useState(hostLobby(players = List(10) { PlayerUi("p$it", "Player $it", PlayerColour.entries[it]) }))
            }

            WHEN("the lobby is shown") {
                show()
            }

            THEN("it shows ten of ten players and no open seats") {
                lobbyRobot.assertPlayerCount("10 / 10 PLAYERS")
                lobbyRobot.assertNoSeatsOpen()
            }
        }

    @Test
    fun `given one seat left when the lobby is shown then the open seat is counted in the singular`() =
        componentTest {
            GIVEN("nine of the ten seats are taken") {
                useState(hostLobby(players = List(9) { PlayerUi("p$it", "Player $it", PlayerColour.entries[it]) }))
            }

            WHEN("the lobby is shown") {
                show()
            }

            THEN("one seat is shown open") {
                lobbyRobot.assertSeatsOpen(1)
            }
        }

    @Test
    fun `should leave the opener to chance when the host has not chosen one`() =
        componentTest {
            GIVEN("the host has not chosen an opener") {
                useState(hostLobby())
            }

            WHEN("the lobby is shown") {
                show()
            }

            THEN("a random player opens and the random chip is selected") {
                lobbyRobot.assertOpenerChoice("Random player")
                lobbyRobot.assertOpenerSelected("Random")
            }
        }

    @Test
    fun `should mark the chosen opener when the host has chosen a player`() =
        componentTest {
            GIVEN("the host has chosen Sam to open") {
                useState(hostLobby().copy(openingPlayerId = sam.id))
            }

            WHEN("the lobby is shown") {
                show()
            }

            THEN("Sam's chip is selected") {
                lobbyRobot.assertOpenerSelected("Sam")
            }
        }

    @Test
    fun `given the host has chosen themselves to open when the lobby is shown then they are named You`() =
        componentTest {
            GIVEN("Alex hosts and has chosen to open") {
                useState(hostLobby().copy(openingPlayerId = alex.id))
            }

            WHEN("the lobby is shown") {
                show()
            }

            THEN("Alex's choice and chip read You") {
                lobbyRobot.assertOpenerChoice("You")
                lobbyRobot.assertOpenerSelected("You")
            }
        }

    @Test
    fun `should show the rounds and disable fewer rounds when the rounds are at the minimum`() =
        componentTest {
            GIVEN("the game is set to one round, the minimum") {
                useState(hostLobby(maxRounds = 1, canDecreaseRounds = false))
            }

            WHEN("the lobby is shown") {
                show()
            }

            THEN("it summarises one round and fewer rounds is disabled") {
                lobbyRobot.assertRoundsSummary("Everyone writes 1 time · 2 turns")
                lobbyRobot.assertFewerRoundsEnabled(false)
            }
        }

    @Test
    fun `should disable more rounds when the rounds are at the maximum`() =
        componentTest {
            GIVEN("the game is set to ten rounds, the maximum") {
                useState(hostLobby(maxRounds = 10, canDecreaseRounds = true, canIncreaseRounds = false))
            }

            WHEN("the lobby is shown") {
                show()
            }

            THEN("it summarises ten rounds and more rounds is disabled") {
                lobbyRobot.assertRoundsSummary("Everyone writes 10 times · 20 turns")
                lobbyRobot.assertMoreRoundsEnabled(false)
            }
        }

    @Test
    fun `should show the host controls when the viewer hosts`() =
        componentTest {
            GIVEN("Alex hosts the lobby") {
                useState(hostLobby())
            }

            WHEN("Alex views the lobby") {
                show()
            }

            THEN("Alex can choose the opener and rounds and start") {
                lobbyRobot.assertHostControlsShown()
            }
        }

    @Test
    fun `should emit the chosen opener when a player's chip is pressed`() =
        componentTest {
            GIVEN("Alex's lobby is showing") {
                useState(hostLobby())
                show()
            }

            WHEN("Alex chooses Sam to open") {
                lobbyRobot.selectOpener("Sam")
            }

            THEN("Sam is emitted as the opener") {
                actions.assertEmitted(LobbyAction.OpeningPlayerSelected(sam.id))
            }
        }

    @Test
    fun `should emit a random opener when the random chip is pressed`() =
        componentTest {
            GIVEN("Alex's lobby is showing with Sam chosen to open") {
                useState(hostLobby().copy(openingPlayerId = sam.id))
                show()
            }

            WHEN("Alex chooses a random opener") {
                lobbyRobot.selectRandomOpener()
            }

            THEN("no particular opener is emitted") {
                actions.assertEmitted(LobbyAction.OpeningPlayerSelected(null))
            }
        }

    @Test
    fun `given one round when more rounds is pressed then increasing the rounds is emitted`() =
        componentTest {
            GIVEN("Alex's lobby is showing one round") {
                useState(hostLobby(maxRounds = 1))
                show()
            }

            WHEN("Alex presses more rounds") {
                lobbyRobot.tapMoreRounds()
            }

            THEN("increasing the rounds is emitted") {
                actions.assertEmitted(LobbyAction.IncreaseRounds)
            }
        }

    @Test
    fun `should emit start game when start is pressed`() =
        componentTest {
            GIVEN("Alex's lobby is showing") {
                useState(hostLobby())
                show()
            }

            WHEN("Alex presses start") {
                lobbyRobot.tapStartGame()
            }

            THEN("the start game action is emitted") {
                actions.assertEmitted(LobbyAction.StartGame)
            }
        }

    @Test
    fun `should emit leave when leave lobby is pressed`() =
        componentTest {
            GIVEN("Alex's lobby is showing") {
                useState(hostLobby())
                show()
            }

            WHEN("Alex presses leave lobby") {
                lobbyRobot.tapLeave()
            }

            THEN("the leave action is emitted") {
                actions.assertEmitted(LobbyAction.Leave)
            }
        }

    @Test
    fun `should wait for the host without host controls when a guest views the lobby`() =
        componentTest {
            GIVEN("Sam is a guest in Alex's lobby") {
                useState(guestLobby())
            }

            WHEN("Sam views the lobby") {
                show()
            }

            THEN("Sam waits for Alex, sees their own seat tagged, and has no host controls") {
                lobbyRobot.assertWaitingForHost("Alex")
                lobbyRobot.assertYouTagShown()
                lobbyRobot.assertHostControlsNotShown()
            }
        }

    @Test
    fun `should show the reconnecting banner when the connection has dropped`() =
        componentTest {
            GIVEN("the connection has dropped") {
                useState(hostLobby(isReconnecting = true))
            }

            WHEN("the lobby is shown") {
                show()
            }

            THEN("the reconnecting banner is shown") {
                lobbyRobot.assertReconnectingShown()
            }
        }

    @Test
    fun `given one word each is off when the host turns it on then turning it on is emitted`() =
        componentTest {
            GIVEN("Alex's lobby is showing with one word each off") {
                useState(hostLobby())
                show()
                lobbyRobot.assertOneWordEach(on = false)
            }

            WHEN("Alex turns on one word each") {
                lobbyRobot.tapOneWordEach()
            }

            THEN("turning on one word each is emitted") {
                actions.assertEmitted(LobbyAction.OneWordPerTurnChanged(enabled = true))
            }
        }

    @Test
    fun `given one word each is on when the lobby is shown then the switch is on`() =
        componentTest {
            GIVEN("Alex has turned on one word each") {
                useState(hostLobby(oneWordPerTurn = true))
            }

            WHEN("the lobby is shown") {
                show()
            }

            THEN("the one word each switch is on") {
                lobbyRobot.assertOneWordEach(on = true)
            }
        }
}
