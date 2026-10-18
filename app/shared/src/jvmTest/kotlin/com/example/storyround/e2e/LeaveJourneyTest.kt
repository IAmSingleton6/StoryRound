package com.example.storyround.e2e

import com.example.storyround.testing.AND
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.FullStackTest
import com.example.storyround.testing.scenario.appHostingLobby
import kotlin.test.Test

class LeaveJourneyTest : FullStackTest() {
    @Test
    fun `should hand hosting to the next player when the host leaves the lobby`() =
        fullStackTest {
            GIVEN("Alex is hosting a lobby with Sam from the app") {
                appHostingLobby(host = "Alex", guests = listOf("Sam"))
            }

            WHEN("Alex leaves the lobby") {
                lobbyRobot.tapLeave()
            }

            THEN("Sam becomes the host") {
                player("Sam").assertHost("Sam")
            }

            AND("Alex is back on Home") {
                homeRobot.assertShown()
            }
        }
}
