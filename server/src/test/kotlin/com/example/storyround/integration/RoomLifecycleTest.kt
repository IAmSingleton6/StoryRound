package com.example.storyround.integration

import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.gameFinished
import com.example.storyround.testing.roomInLobby
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

class RoomLifecycleTest : ServerIntegrationTest() {
    @Test
    fun `should discard the room when nobody has been connected for ten minutes`() =
        serverTest {
            GIVEN("Alex's lobby has had nobody connected for ten minutes") {
                roomInLobby(host = "Alex")
                clock.awaitTimerScheduledBy { player("Alex").disconnect() }
                clock.advanceBy(10.minutes)
            }

            WHEN("Alex reconnects") {
                player("Alex").reconnect()
            }

            THEN("Alex is told the room no longer exists") {
                player("Alex").assertErrorReceived("ROOM_NOT_FOUND")
            }
        }

    @Test
    fun `should discard a finished room when everyone went home ten minutes ago`() =
        serverTest {
            GIVEN("Alex and Sam went home from a finished game ten minutes ago") {
                gameFinished(host = "Alex", guests = listOf("Sam"))
                clock.awaitTimerScheduledBy {
                    player("Alex").disconnect()
                    player("Sam").disconnect()
                }
                clock.advanceBy(10.minutes)
            }

            WHEN("Sam reconnects") {
                player("Sam").reconnect()
            }

            THEN("Sam is told the room no longer exists") {
                player("Sam").assertErrorReceived("ROOM_NOT_FOUND")
            }
        }

    @Test
    fun `should keep the room when its player reconnects within ten minutes`() =
        serverTest {
            GIVEN("Alex reconnected nine minutes after the lobby emptied, and two more minutes have passed") {
                roomInLobby(host = "Alex")
                clock.awaitTimerScheduledBy { player("Alex").disconnect() }
                clock.advanceBy(9.minutes)
                player("Alex").reconnect()
                clock.advanceBy(2.minutes)
            }

            WHEN("Sam joins Alex's room") {
                player("Sam").joinRoom(player("Alex").roomCode)
            }

            THEN("Sam is seated with Alex") {
                player("Sam").assertPlayers("Alex", "Sam")
            }
        }

    @Test
    fun `should keep the room when someone joins within ten minutes`() =
        serverTest {
            GIVEN("Sam joined nine minutes after Alex's lobby emptied, and two more minutes have passed") {
                roomInLobby(host = "Alex")
                clock.awaitTimerScheduledBy { player("Alex").disconnect() }
                clock.advanceBy(9.minutes)
                player("Sam").joinRoom(player("Alex").roomCode)
                clock.advanceBy(2.minutes)
            }

            WHEN("Alex reconnects") {
                player("Alex").reconnect()
            }

            THEN("Alex is back in the room with Sam") {
                player("Alex").assertPlayers("Alex", "Sam")
            }
        }

    @Test
    fun `should keep the room open when one player is still connected`() =
        serverTest {
            GIVEN("Sam has been disconnected from Alex's lobby for an hour while Alex stayed") {
                roomInLobby(host = "Alex", guests = listOf("Sam"))
                player("Sam").disconnect()
                clock.advanceBy(60.minutes)
            }

            WHEN("Sam reconnects") {
                player("Sam").reconnect()
            }

            THEN("Sam is back in the room with Alex") {
                player("Sam").assertPlayers("Alex", "Sam")
            }
        }
}
