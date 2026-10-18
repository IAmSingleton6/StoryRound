package com.example.storyround.testing.robot

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.performClick
import com.example.storyround.testing.robot.component.BannerRobot

class HomeRobot(
    ui: ComposeUiTest,
) : ScreenRobot(ui) {
    private val banner = BannerRobot(ui)

    override val screenMarker = hasContentDescription(NAME_FIELD)

    fun enterName(name: String) = onScreen { typeInto(NAME_FIELD, name) }

    fun enterRoomCode(code: String) = onScreen { typeInto(ROOM_CODE_FIELD, code) }

    fun tapCreateGame() = onScreen { tap(CREATE_GAME) }

    fun tapJoinGame() = onScreen { tap(JOIN_GAME) }

    fun tapCancel() = onScreen { tap("Cancel") }

    fun tapHistory() = onScreen { await(hasContentDescription(HISTORY, substring = true) and hasClickAction()).performClick() }

    fun assertCreateEnabled(enabled: Boolean) = onScreen { assertControlEnabled(CREATE_GAME, enabled) }

    fun assertCreateNotShown() = onScreen { assertTextNotShown(CREATE_GAME) }

    fun assertJoinEnabled(enabled: Boolean) = onScreen { assertControlEnabled(JOIN_GAME, enabled) }

    fun assertConnecting() = onScreen { assertControlEnabled(CONNECTING, enabled = false) }

    fun assertRoomCodeFieldShown() = onScreen { assertDescriptionShown(ROOM_CODE_FIELD) }

    fun assertRoomCodePlaceholderShown() = onScreen { assertTextShown("ABCD") }

    fun assertErrorShown(message: String) = onScreen { banner.assertErrorShown(message) }

    fun assertNoErrorShown(message: String) = onScreen { assertTextNotShown(message) }

    fun assertHistoryCount(announcement: String) = onScreen { assertDescriptionShown("$HISTORY, $announcement") }

    private companion object {
        const val NAME_FIELD = "Your name"
        const val ROOM_CODE_FIELD = "Room code"
        const val CREATE_GAME = "CREATE GAME"
        const val JOIN_GAME = "JOIN GAME"
        const val CONNECTING = "CONNECTING…"
        const val HISTORY = "Story history"
    }
}
