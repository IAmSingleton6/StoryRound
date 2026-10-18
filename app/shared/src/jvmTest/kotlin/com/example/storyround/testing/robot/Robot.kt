package com.example.storyround.testing.robot

import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToString
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import androidx.compose.ui.test.waitUntilDoesNotExist
import com.example.storyround.testing.ROBOT_TIMEOUT

abstract class Robot(
    protected val ui: ComposeUiTest,
) {
    protected fun await(matcher: SemanticsMatcher): SemanticsNodeInteraction {
        try {
            ui.waitUntilAtLeastOneExists(matcher, ROBOT_TIMEOUT.inWholeMilliseconds)
        } catch (_: ComposeTimeoutException) {
            throw AssertionError("Expected to see ${matcher.description}. On screen:\n${screenDump()}")
        }
        return ui.onAllNodes(matcher).onFirst()
    }

    protected fun awaitGone(matcher: SemanticsMatcher) {
        ui.waitForIdle()
        try {
            ui.waitUntilDoesNotExist(matcher, ROBOT_TIMEOUT.inWholeMilliseconds)
        } catch (_: ComposeTimeoutException) {
            throw AssertionError("Expected not to see ${matcher.description}. On screen:\n${screenDump()}")
        }
    }

    protected fun tap(label: String) {
        await(hasText(label) and hasClickAction()).performClick()
    }

    protected fun tapDescribed(description: String) {
        await(hasContentDescription(description) and hasClickAction()).performClick()
    }

    protected fun typeInto(
        field: String,
        text: String,
    ) {
        await(hasContentDescription(field)).performTextInput(text)
    }

    protected fun assertTextShown(
        text: String,
        substring: Boolean = false,
    ) {
        await(hasText(text, substring = substring))
    }

    protected fun assertTextNotShown(text: String) = awaitGone(hasText(text))

    protected fun assertDescriptionShown(description: String) {
        await(hasContentDescription(description))
    }

    protected fun assertControlEnabled(
        label: String,
        enabled: Boolean,
    ) {
        await(hasText(label) and if (enabled) isEnabled() else isNotEnabled())
    }

    protected fun assertAnnounced(
        text: String,
        mode: LiveRegionMode,
    ) {
        await(hasLiveRegion(mode) and (hasText(text) or hasAnyDescendant(hasText(text))))
    }

    private fun hasLiveRegion(mode: LiveRegionMode) =
        SemanticsMatcher("is a $mode live region") { it.config.getOrNull(SemanticsProperties.LiveRegion) == mode }

    private fun screenDump(): String = runCatching { ui.onRoot().printToString() }.getOrDefault("(nothing rendered)")

    companion object {
        fun hasTextMatching(regex: Regex): SemanticsMatcher =
            SemanticsMatcher("has text matching $regex") { node ->
                node.config
                    .getOrNull(SemanticsProperties.Text)
                    .orEmpty()
                    .any { regex.matches(it.text) }
            }
    }
}

abstract class ScreenRobot(
    ui: ComposeUiTest,
) : Robot(ui) {
    protected abstract val screenMarker: SemanticsMatcher

    protected fun <T> onScreen(step: () -> T): T {
        await(screenMarker)
        return step()
    }

    fun assertShown() = onScreen { }
}
