package com.example.storyround.ui.components

import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.ComponentTest
import kotlin.test.Test

class ErrorBannerTest : ComponentTest<String, Nothing>({ message, _ -> ErrorBanner(message) }) {
    @Test
    fun `should announce the message straight away when an error is shown`() =
        componentTest {
            GIVEN("the room could not be found") {
                useState(MESSAGE)
            }

            WHEN("the error banner is shown") {
                show()
            }

            THEN("the message is announced assertively") {
                bannerRobot.assertErrorShown(MESSAGE)
            }
        }

    private companion object {
        const val MESSAGE = "No game found with that code. Check it and try again."
    }
}
