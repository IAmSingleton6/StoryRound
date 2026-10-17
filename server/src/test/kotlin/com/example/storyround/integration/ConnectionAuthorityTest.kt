package com.example.storyround.integration

import com.example.storyround.testing.AND
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.roomInLobby
import kotlin.test.Test

class ConnectionAuthorityTest : ServerIntegrationTest() {
    @Test
    fun `should refuse the command when a guest uses the host's id`() =
        serverTest {
            GIVEN("Sam is a guest in Alex's lobby") {
                roomInLobby(host = "Alex", guests = listOf("Sam"))
            }

            WHEN("Sam tries to start the game as Alex") {
                player("Sam").startGameAs("Alex")
            }

            THEN("Sam is told the connection is not playing as Alex") {
                player("Sam").assertErrorReceived("NOT_CONNECTED_AS_PLAYER")
            }

            AND("Alex's game does not start") {
                player("Alex").assertNoMoreMessages()
            }
        }

    @Test
    fun `should refuse the command when the connection never joined the room`() =
        serverTest {
            GIVEN("Alex is hosting a lobby and Mallory is connected without joining it") {
                roomInLobby(host = "Alex")
                player("Mallory").connect()
            }

            WHEN("Mallory tries to start Alex's game as Alex") {
                player("Mallory").startGameAs("Alex")
            }

            THEN("Mallory is told the connection is not playing as Alex") {
                player("Mallory").assertErrorReceived("NOT_CONNECTED_AS_PLAYER")
            }
        }

    @Test
    fun `should refuse the command when it names another room`() =
        serverTest {
            GIVEN("Alex and Sam each host their own lobby") {
                roomInLobby(host = "Alex")
                roomInLobby(host = "Sam")
            }

            WHEN("Alex sends a start for Sam's room using Alex's own id") {
                player("Alex").startGameAs("Alex", inRoom = player("Sam").roomCode)
            }

            THEN("Alex is told the connection is not playing in that room") {
                player("Alex").assertErrorReceived("NOT_CONNECTED_AS_PLAYER")
            }
        }
}
