package com.example.storyround.integration

import com.example.storyround.protocol.GameStatus
import com.example.storyround.testing.AND
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.gameAwaitingPrompt
import com.example.storyround.testing.gameFinished
import com.example.storyround.testing.gameInPlay
import com.example.storyround.testing.roomInLobby
import kotlin.test.Test

class ReconnectTest : ServerIntegrationTest() {
    @Test
    fun `should put the player back in their lobby seat when they reconnect`() =
        serverTest {
            GIVEN("Alex has lost connection to a lobby with Sam") {
                roomInLobby(host = "Alex", guests = listOf("Sam"))
                player("Alex").disconnect()
            }

            WHEN("Alex reconnects") {
                player("Alex").reconnect()
            }

            THEN("Alex is back in the same seat, still hosting") {
                player("Alex").assertPlayers("Alex", "Sam")
                player("Alex").assertHost("Alex")
            }
        }

    @Test
    fun `should show the game waiting for the prompt when the opener reconnects`() =
        serverTest {
            GIVEN("Sam lost connection while being asked for the opening prompt") {
                gameAwaitingPrompt(host = "Alex", guests = listOf("Sam"), opener = "Sam")
                player("Sam").disconnect()
            }

            WHEN("Sam reconnects") {
                player("Sam").reconnect()
            }

            THEN("Sam is still being asked for the prompt") {
                player("Sam").assertRoom("Sam opening") {
                    it.status == GameStatus.Prompt && it.openingPlayerId == player("Sam").playerId
                }
            }
        }

    @Test
    fun `should show the current turn when a player reconnects mid-game`() =
        serverTest {
            GIVEN("Sam lost connection during Alex's turn") {
                gameInPlay(host = "Alex", guests = listOf("Sam"))
                player("Sam").disconnect()
            }

            WHEN("Sam reconnects") {
                player("Sam").reconnect()
            }

            THEN("Sam sees it is still Alex's first turn") {
                player("Sam").assertCurrentTurn(of = "Alex", number = 0)
            }
        }

    @Test
    fun `should send the finished story again when a player reconnects after the end`() =
        serverTest {
            GIVEN("Alex lost connection after finishing a solo game") {
                gameFinished(host = "Alex", prompt = "A door creaked open.", lines = listOf("The end."))
                player("Alex").disconnect()
            }

            WHEN("Alex reconnects") {
                player("Alex").reconnect()
            }

            THEN("Alex is sent the whole story again") {
                player("Alex").assertStoryFinished("A door creaked open.", "The end.")
            }
        }

    @Test
    fun `should tell only the returning player when a player reconnects`() =
        serverTest {
            GIVEN("Sam has lost connection to a lobby with Alex") {
                roomInLobby(host = "Alex", guests = listOf("Sam"))
                player("Sam").disconnect()
            }

            WHEN("Sam reconnects") {
                player("Sam").reconnect()
            }

            THEN("Alex is not sent anything") {
                player("Alex").assertNoMoreMessages()
            }
        }

    @Test
    fun `should refuse the reconnect when the session token is wrong`() =
        serverTest {
            GIVEN("Alex has lost connection to a lobby") {
                roomInLobby(host = "Alex")
                player("Alex").disconnect()
            }

            WHEN("Alex reconnects with someone else's token") {
                player("Alex").reconnectWithToken("not-alex")
            }

            THEN("Alex is told the session cannot reconnect") {
                player("Alex").assertErrorReceived("INVALID_SESSION")
            }
        }

    @Test
    fun `should refuse the reconnect when the player has left the room`() =
        serverTest {
            GIVEN("Sam left Alex's lobby and then lost connection") {
                roomInLobby(host = "Alex", guests = listOf("Sam"))
                player("Sam").leaveRoom()
                player("Sam").disconnect()
            }

            WHEN("Sam reconnects with the old session") {
                player("Sam").reconnect()
            }

            THEN("Sam is told the player is no longer in the room") {
                player("Sam").assertErrorReceived("PLAYER_NOT_FOUND")
            }
        }

    @Test
    fun `should refuse the old connection's commands when a player reconnects on another device`() =
        serverTest {
            GIVEN("it is Alex's turn and Alex has reconnected on a second device") {
                gameInPlay(host = "Alex", guests = listOf("Sam"))
                player("Alex").otherDevice.reconnect()
            }

            WHEN("Alex's first device submits Alex's turn") {
                player("Alex").trySubmitContribution("From the old phone.")
            }

            THEN("the first device is told it is no longer playing as Alex") {
                player("Alex").assertErrorReceived("NOT_CONNECTED_AS_PLAYER")
            }
        }

    @Test
    fun `should send room updates only to the new connection when a player reconnects on another device`() =
        serverTest {
            GIVEN("Alex has reconnected to a lobby with Sam on a second device") {
                roomInLobby(host = "Alex", guests = listOf("Sam"))
                player("Alex").otherDevice.reconnect()
            }

            WHEN("Jo joins the room") {
                player("Jo").joinRoom(player("Alex").roomCode)
            }

            THEN("Alex's second device is told Jo arrived") {
                player("Alex").otherDevice.assertPlayerJoined("Jo")
            }

            AND("Alex's first device hears nothing") {
                player("Alex").assertNoMoreMessages()
            }
        }

    @Test
    fun `should let the new connection play when a player reconnects on another device`() =
        serverTest {
            GIVEN("it is Alex's turn and Alex has reconnected on a second device") {
                gameInPlay(host = "Alex", guests = listOf("Sam"))
                player("Alex").otherDevice.reconnect()
            }

            WHEN("Alex submits the turn from the second device") {
                player("Alex").otherDevice.submitContribution("From the new phone.")
            }

            THEN("Sam receives Alex's contribution") {
                player("Sam").assertContributionAdded(by = "Alex", text = "From the new phone.")
            }
        }
}
