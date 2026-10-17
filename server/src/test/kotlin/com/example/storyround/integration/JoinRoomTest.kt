package com.example.storyround.integration

import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.testing.AND
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.gameAwaitingPrompt
import com.example.storyround.testing.roomInLobby
import kotlin.test.Test

class JoinRoomTest : ServerIntegrationTest() {
    @Test
    fun `should seat the new player for everyone when a player joins`() =
        serverTest {
            GIVEN("Alex is hosting a lobby") {
                roomInLobby(host = "Alex")
            }

            WHEN("Sam joins with the room code") {
                player("Sam").joinRoom(player("Alex").roomCode)
            }

            THEN("Alex and Sam both see the same two seats") {
                player("Alex").assertPlayers("Alex", "Sam")
                player("Sam").assertPlayers("Alex", "Sam")
            }

            AND("Alex is told Sam arrived") {
                player("Alex").assertPlayerJoined("Sam")
            }
        }

    @Test
    fun `should accept the room code when it is typed in lower case`() =
        serverTest {
            GIVEN("Alex is hosting a lobby") {
                roomInLobby(host = "Alex")
            }

            WHEN("Sam joins with the room code in lower case") {
                player("Sam").joinRoom(player("Alex").roomCode.lowercase())
            }

            THEN("Sam is seated in Alex's room") {
                player("Sam").assertPlayers("Alex", "Sam")
            }
        }

    @Test
    fun `should give every player a different colour when several players join`() =
        serverTest {
            GIVEN("Alex is hosting a lobby with Sam and Jo") {
                roomInLobby(host = "Alex", guests = listOf("Sam", "Jo"))
            }

            WHEN("Kim joins") {
                player("Kim").joinRoom(player("Alex").roomCode)
            }

            THEN("all four players have different colours") {
                player("Kim").assertEveryoneHasADifferentColour()
            }
        }

    @Test
    fun `should reuse a leaver's colour when the next player joins`() =
        serverTest {
            GIVEN("Sam, who was green, has left Alex's lobby") {
                roomInLobby(host = "Alex", guests = listOf("Sam", "Jo"))
                player("Sam").leaveRoom()
            }

            WHEN("Kim joins") {
                player("Kim").joinRoom(player("Alex").roomCode)
            }

            THEN("Kim is green") {
                player("Kim").assertColourOf("Kim", PlayerColour.Green)
            }
        }

    @Test
    fun `should refuse the join when no room has that code`() =
        serverTest {
            GIVEN("Sam is connected and no rooms exist") {
                player("Sam").connect()
            }

            WHEN("Sam joins room ZZZZ") {
                player("Sam").joinRoom("ZZZZ")
            }

            THEN("Sam is told there is no such room") {
                player("Sam").assertErrorReceived("ROOM_NOT_FOUND")
            }
        }

    @Test
    fun `should refuse the join when the game has already started`() =
        serverTest {
            GIVEN("Alex has started a game") {
                gameAwaitingPrompt(host = "Alex")
            }

            WHEN("Sam joins Alex's room") {
                player("Sam").joinRoom(player("Alex").roomCode)
            }

            THEN("Sam is told the game has started") {
                player("Sam").assertErrorReceived("GAME_ALREADY_STARTED")
            }
        }

    @Test
    fun `should refuse the join when the room is full`() =
        serverTest(configuration = GameConfiguration(maxPlayers = 2)) {
            GIVEN("Alex and Sam fill a two-seat room") {
                roomInLobby(host = "Alex", guests = listOf("Sam"))
            }

            WHEN("Jo joins the room") {
                player("Jo").joinRoom(player("Alex").roomCode)
            }

            THEN("Jo is told the room is full") {
                player("Jo").assertErrorReceived("ROOM_FULL")
            }
        }

    @Test
    fun `should refuse the join when the name is invalid`() =
        serverTest {
            GIVEN("Alex is hosting a lobby") {
                roomInLobby(host = "Alex")
            }

            WHEN("Sam joins as \"S\"") {
                player("Sam").joinRoom(player("Alex").roomCode, withName = "S")
            }

            THEN("Sam is told the name is invalid") {
                player("Sam").assertErrorReceived("INVALID_NAME")
            }
        }
}
