package com.example.storyround.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

private val MidnightPaperMaterialColors =
    lightColorScheme(
        primary = MidnightPaperColors.ink,
        onPrimary = MidnightPaperColors.paperWhite,
        background = MidnightPaperColors.stage,
        onBackground = MidnightPaperColors.onStage,
        surface = MidnightPaperColors.stage,
        onSurface = MidnightPaperColors.onStage,
    )

@Immutable
internal data class StoryRoundTokens(
    val colors: StoryRoundColors,
    val typography: StoryRoundTypography,
)

private val LocalStoryRoundTokens =
    staticCompositionLocalOf<StoryRoundTokens> {
        error("StoryRoundTokens are only available inside StoryRoundTheme.")
    }

internal object StoryRoundTheme {
    val colors: StoryRoundColors
        @Composable
        @ReadOnlyComposable
        get() = LocalStoryRoundTokens.current.colors

    val typography: StoryRoundTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalStoryRoundTokens.current.typography
}

@Composable
internal fun StoryRoundTheme(content: @Composable () -> Unit) {
    val fonts = storyRoundFonts()
    val tokens = remember(fonts) { StoryRoundTokens(MidnightPaperColors, storyRoundTypography(fonts)) }
    CompositionLocalProvider(LocalStoryRoundTokens provides tokens) {
        MaterialTheme(
            colorScheme = MidnightPaperMaterialColors,
            content = content,
        )
    }
}
