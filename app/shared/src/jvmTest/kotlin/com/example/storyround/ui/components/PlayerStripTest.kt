package com.example.storyround.ui.components

import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.ComponentTest
import com.example.storyround.testing.fixtures.alex
import com.example.storyround.testing.fixtures.sam
import kotlin.test.Test

class PlayerStripTest : ComponentTest<String?, Nothing>({ activePlayerId, _ -> PlayerStrip(listOf(alex, sam), activePlayerId) }) {
    @Test
    fun `should name every player when the strip is shown`() =
        componentTest {
            GIVEN("nobody is writing yet") {
                useState(null)
            }

            WHEN("the strip of Alex and Sam is shown") {
                show()
            }

            THEN("both players are named") {
                playerStripRobot.assertPlayerShown("Alex")
                playerStripRobot.assertPlayerShown("Sam")
            }
        }

    @Test
    fun `should announce the active writer as writing now when someone has the turn`() =
        componentTest {
            GIVEN("Sam has the turn") {
                useState(sam.id)
            }

            WHEN("the strip of Alex and Sam is shown") {
                show()
            }

            THEN("Sam is announced as writing now") {
                playerStripRobot.assertWriting("Sam")
            }
        }
}
