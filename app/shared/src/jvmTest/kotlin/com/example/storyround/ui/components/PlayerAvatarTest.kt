package com.example.storyround.ui.components

import androidx.compose.ui.unit.dp
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.ComponentTest
import com.example.storyround.testing.fixtures.sam
import kotlin.test.Test

internal class PlayerAvatarTest : ComponentTest<PlayerUi, Nothing>({ player, _ -> PlayerAvatar(player, size = 38.dp) }) {
    @Test
    fun `should show the player's initial when their avatar is shown`() =
        componentTest {
            GIVEN("the player is Sam") {
                useState(sam)
            }

            WHEN("Sam's avatar is shown") {
                show()
            }

            THEN("it shows S") {
                playerStripRobot.assertInitialShown("S")
            }
        }
}
