package com.example.storyround.ui.components

import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.ComponentTest
import kotlin.test.Test

class ReconnectingBannerTest : ComponentTest<Boolean, Nothing>({ isReconnecting, _ -> if (isReconnecting) ReconnectingBanner() }) {
    @Test
    fun `should tell the player their seat is saved when the connection has dropped`() =
        componentTest {
            GIVEN("the connection has dropped") {
                useState(true)
            }

            WHEN("a screen shows the banner while reconnecting, as every room screen does") {
                show()
            }

            THEN("it politely says the seat is saved") {
                bannerRobot.assertReconnectingShown()
            }
        }
}
