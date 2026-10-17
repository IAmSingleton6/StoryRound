package com.example.storyround.integration

import com.example.storyround.testing.AND
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.gameAwaitingPrompt
import com.example.storyround.testing.roomInLobby
import kotlin.test.Test

class LeaveRoomTest : ServerIntegrationTest() {
    @Test
    fun `should tell everyone and free the seat when a guest leaves the lobby`() =
        serverTest {
            GIVEN("Alex and Sam are in a lobby") {
                roomInLobby(host = "Alex", guests = listOf("Sam"))
            }

            WHEN("Sam leaves") {
                player("Sam").leaveRoom()
            }

            THEN("Alex is told Sam left") {
                player("Alex").assertPlayerLeft("Sam")
            }

            AND("only Alex is still seated") {
                player("Alex").assertPlayers("Alex")
            }
        }

    @Test
    fun `should refuse the leaver's commands when they have left`() =
        serverTest {
            GIVEN("Sam has left Alex's lobby") {
                roomInLobby(host = "Alex", guests = listOf("Sam"))
                player("Sam").leaveRoom()
            }

            WHEN("Sam tries to leave again") {
                player("Sam").leaveRoom()
            }

            THEN("Sam is told the connection is no longer playing") {
                player("Sam").assertErrorReceived("NOT_CONNECTED_AS_PLAYER")
            }
        }

    @Test
    fun `should make the next player host when the host leaves`() =
        serverTest {
            GIVEN("Alex hosts a lobby with Sam and Jo") {
                roomInLobby(host = "Alex", guests = listOf("Sam", "Jo"))
            }

            WHEN("Alex leaves") {
                player("Alex").leaveRoom()
            }

            THEN("Sam becomes host for everyone left") {
                player("Sam").assertHost("Sam")
                player("Jo").assertHost("Sam")
            }
        }

    @Test
    fun `should close the room when the last player leaves`() =
        serverTest {
            GIVEN("Alex has left the lobby Alex was alone in") {
                roomInLobby(host = "Alex")
                player("Alex").leaveRoom()
            }

            WHEN("Sam tries to join that room") {
                player("Sam").joinRoom(player("Alex").roomCode)
            }

            THEN("Sam is told the room no longer exists") {
                player("Sam").assertErrorReceived("ROOM_NOT_FOUND")
            }
        }

    @Test
    fun `should refuse to let a player leave when the game has started`() =
        serverTest {
            GIVEN("Alex has started a game with Sam") {
                gameAwaitingPrompt(host = "Alex", guests = listOf("Sam"))
            }

            WHEN("Sam leaves") {
                player("Sam").leaveRoom()
            }

            THEN("Sam is told leaving is only allowed in the lobby") {
                player("Sam").assertErrorReceived("CANNOT_LEAVE_GAME")
            }
        }
}
