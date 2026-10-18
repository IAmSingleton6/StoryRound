package com.example.storyround.e2e

import com.example.storyround.random.RandomNumberGenerator
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.FullStackTest
import com.example.storyround.testing.scenario.appHostingLobby
import io.mockk.every
import io.mockk.mockk
import kotlin.random.Random
import kotlin.test.Test

class RandomOpenerJourneyTest : FullStackTest() {
    @Test
    fun `should show the server's choice of opener when the host leaves it to chance`() =
        fullStackTest(randomNumberGenerator = pickingTheSecondOfTwo()) {
            GIVEN("Alex hosts a lobby with Sam, the opener is left to chance, and the server's pick lands on Sam") {
                appHostingLobby(host = "Alex", guests = listOf("Sam"))
                lobbyRobot.selectRandomOpener()
            }

            WHEN("Alex starts the game") {
                lobbyRobot.tapStartGame()
            }

            THEN("Alex waits for Sam's opening prompt") {
                openingPromptRobot.assertWaitingFor("Sam")
            }
        }

    private fun pickingTheSecondOfTwo(): RandomNumberGenerator =
        mockk {
            every { nextInt(any()) } answers { Random.nextInt(firstArg<Int>()) }
            every { nextInt(2) } returns 1
        }
}
