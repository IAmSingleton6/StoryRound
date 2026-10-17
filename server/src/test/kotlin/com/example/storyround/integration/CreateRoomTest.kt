package com.example.storyround.integration

import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.roomInLobby
import kotlin.test.Test

class CreateRoomTest : ServerIntegrationTest() {
    @Test
    fun `should make the creator host of a new lobby when they create a room`() =
        serverTest {
            GIVEN("Alex is connected") {
                player("Alex").connect()
            }

            WHEN("Alex creates a room") {
                player("Alex").createRoom()
            }

            THEN("Alex hosts a lobby with only Alex in it") {
                player("Alex").assertStatus(GameStatus.Lobby)
                player("Alex").assertHost("Alex")
                player("Alex").assertPlayers("Alex")
            }
        }

    @Test
    fun `should give the creator the first colour when they create a room`() =
        serverTest {
            GIVEN("Alex is connected") {
                player("Alex").connect()
            }

            WHEN("Alex creates a room") {
                player("Alex").createRoom()
            }

            THEN("Alex is blue") {
                player("Alex").assertColourOf("Alex", PlayerColour.Blue)
            }
        }

    @Test
    fun `should tidy the name before sharing it when the name has extra spacing`() =
        serverTest {
            GIVEN("Alex is connected") {
                player("Alex").connect()
            }

            WHEN("Alex creates a room as \"  Alex \\n\"") {
                player("Alex").createRoom(withName = "  Alex \n")
            }

            THEN("the room calls the player Alex") {
                player("Alex").assertPlayers("Alex")
            }
        }

    @Test
    fun `should refuse the name when it is shorter than two characters`() =
        serverTest {
            GIVEN("Alex is connected") {
                player("Alex").connect()
            }

            WHEN("Alex creates a room as \"A\"") {
                player("Alex").createRoom(withName = "A")
            }

            THEN("Alex is told the name is invalid") {
                player("Alex").assertErrorReceived("INVALID_NAME")
            }
        }

    @Test
    fun `should refuse the name when it is longer than twenty characters`() =
        serverTest {
            GIVEN("Alex is connected") {
                player("Alex").connect()
            }

            WHEN("Alex creates a room with a 21-character name") {
                player("Alex").createRoom(withName = "A".repeat(21))
            }

            THEN("Alex is told the name is invalid") {
                player("Alex").assertErrorReceived("INVALID_NAME")
            }
        }

    @Test
    fun `should refuse the name when it is only spaces and control characters`() =
        serverTest {
            GIVEN("Alex is connected") {
                player("Alex").connect()
            }

            WHEN("Alex creates a room with a blank name") {
                player("Alex").createRoom(withName = " \t\u0007 ")
            }

            THEN("Alex is told the name is invalid") {
                player("Alex").assertErrorReceived("INVALID_NAME")
            }
        }

    @Test
    fun `should give each room its own code when two players create rooms`() =
        serverTest {
            GIVEN("Alex has created a room") {
                roomInLobby(host = "Alex")
            }

            WHEN("Sam creates another room") {
                player("Sam").createRoom()
            }

            THEN("Sam's room has a different code") {
                player("Sam").assertInDifferentRoomFrom("Alex")
            }
        }
}
