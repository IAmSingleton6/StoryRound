package com.example.storyround.testing.robot.component

import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.test.ComposeUiTest
import com.example.storyround.testing.robot.Robot

class BannerRobot(
    ui: ComposeUiTest,
) : Robot(ui) {
    fun assertErrorShown(message: String) = assertAnnounced(message, LiveRegionMode.Assertive)

    fun assertReconnectingShown() = assertAnnounced(RECONNECTING, LiveRegionMode.Polite)

    fun assertReconnectingNotShown() = assertTextNotShown(RECONNECTING)

    fun assertWaitingShown(status: String) = assertAnnounced(status, LiveRegionMode.Polite)

    private companion object {
        const val RECONNECTING = "Reconnecting… your seat is saved"
    }
}
