package com.example.storyround.testing.robot

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.storyround.testing.robot.component.BannerRobot

class LobbyRobot(
    ui: ComposeUiTest,
) : ScreenRobot(ui) {
    private val banner = BannerRobot(ui)

    override val screenMarker = hasText("LOBBY")

    fun readRoomCode(): String =
        onScreen {
            await(ROOM_CODE)
                .fetchSemanticsNode()
                .config
                .getOrNull(SemanticsProperties.Text)
                .orEmpty()
                .first()
                .text
        }

    fun selectOpener(chipLabel: String) = onScreen { await(hasText(chipLabel) and hasClickAction()).performClick() }

    fun selectRandomOpener() = selectOpener(RANDOM)

    fun tapMoreRounds() = onScreen { tapDescribed(MORE_ROUNDS) }

    fun tapFewerRounds() = onScreen { tapDescribed(FEWER_ROUNDS) }

    fun tapStartGame() = onScreen { tap(START_GAME) }

    fun tapOneWordEach() = onScreen { await(hasText(ONE_WORD_EACH) and hasClickAction()).performScrollTo().performClick() }

    fun tapLeave() = onScreen { tap("Leave lobby") }

    fun assertRoomCodeShown(code: String) = onScreen { assertTextShown(code) }

    fun assertPlayersShown(vararg names: String) =
        onScreen {
            names.forEach { assertTextShown(it) }
            await(hasTextMatching(Regex("${names.size} / \\d+ PLAYERS")))
        }

    fun assertPlayerNotShown(name: String) = onScreen { awaitGone(hasText(name) and !hasClickAction()) }

    fun assertPlayerCount(text: String) = onScreen { assertTextShown(text) }

    fun assertSeatsOpen(count: Int) = onScreen { assertTextShown("$count ${if (count == 1) "seat" else "seats"} open") }

    fun assertNoSeatsOpen() = onScreen { awaitGone(hasText("seats open", substring = true)) }

    fun assertYouTagShown() = onScreen { assertTextShown("YOU") }

    fun assertOpenerChoice(choice: String) = onScreen { assertTextShown(choice) }

    fun assertOpenerSelected(chipLabel: String) = onScreen { await(hasText(chipLabel) and hasClickAction()).assertIsSelected() }

    fun assertRoundsSummary(summary: String) = onScreen { assertTextShown(summary) }

    fun assertOneWordEach(on: Boolean) =
        onScreen {
            val toggle = await(hasText(ONE_WORD_EACH) and hasClickAction()).performScrollTo()
            if (on) toggle.assertIsOn() else toggle.assertIsOff()
        }

    fun assertFewerRoundsEnabled(enabled: Boolean) = onScreen { assertRoundButtonEnabled(FEWER_ROUNDS, enabled) }

    fun assertMoreRoundsEnabled(enabled: Boolean) = onScreen { assertRoundButtonEnabled(MORE_ROUNDS, enabled) }

    fun assertHostControlsShown() =
        onScreen {
            assertTextShown(WHO_OPENS)
            assertTextShown("You are the host. Start when you're ready.")
            assertControlEnabled(START_GAME, enabled = true)
        }

    fun assertHostControlsNotShown() =
        onScreen {
            assertTextNotShown(WHO_OPENS)
            assertTextNotShown(START_GAME)
        }

    fun assertWaitingForHost(host: String) = onScreen { assertTextShown("Waiting for $host to start the game…") }

    fun assertReconnectingShown() = onScreen { banner.assertReconnectingShown() }

    fun assertReconnectingNotShown() = onScreen { banner.assertReconnectingNotShown() }

    private fun assertRoundButtonEnabled(
        description: String,
        enabled: Boolean,
    ) {
        await(hasContentDescription(description) and if (enabled) isEnabled() else isNotEnabled())
    }

    private companion object {
        val ROOM_CODE = hasTextMatching(Regex("[A-Z0-9]{4}"))
        const val RANDOM = "Random"
        const val MORE_ROUNDS = "More rounds"
        const val FEWER_ROUNDS = "Fewer rounds"
        const val START_GAME = "START GAME"
        const val ONE_WORD_EACH = "One word each"
        const val WHO_OPENS = "Who opens the story?"
    }
}
