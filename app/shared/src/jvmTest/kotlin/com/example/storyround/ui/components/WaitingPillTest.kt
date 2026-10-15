package com.example.storyround.ui.components

import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.ComponentTest
import com.example.storyround.testing.fixtures.alex
import kotlin.test.Test

internal class WaitingPillTest : ComponentTest<PlayerUi, Nothing>({ writer, _ -> WaitingPill(writer, "${writer.name} is writing…") }) {
    @Test
    fun `should announce who is writing when someone else has the turn`() =
        componentTest {
            GIVEN("Alex is writing") {
                useState(alex)
            }

            WHEN("the waiting pill is shown") {
                show()
            }

            THEN("it politely announces that Alex is writing") {
                bannerRobot.assertWaitingShown("Alex is writing…")
            }
        }
}
