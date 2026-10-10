package com.example.storyround.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.storyround.protocol.PlayerColour

internal val StoryRoundInk = Color(0xFF1D2130)
private val StoryRoundPaper = Color(0xFFFFFBF5)

private val StoryRoundColorScheme =
    lightColorScheme(
        primary = StoryRoundInk,
        onPrimary = Color.White,
        background = StoryRoundPaper,
        onBackground = StoryRoundInk,
        surface = StoryRoundPaper,
        onSurface = StoryRoundInk,
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
            colorScheme = StoryRoundColorScheme,
            content = content,
        )
    }
}

internal fun PlayerColour.toUiColor(): Color =
    when (this) {
        PlayerColour.Blue -> Color(0xFF377DFF)
        PlayerColour.Green -> Color(0xFF2D9D78)
        PlayerColour.Yellow -> Color(0xFFE8A317)
        PlayerColour.Red -> Color(0xFFE05D5D)
        PlayerColour.Purple -> Color(0xFF8E5CD7)
        PlayerColour.Orange -> Color(0xFFF08A24)
        PlayerColour.Teal -> Color(0xFF1C9AA5)
        PlayerColour.Pink -> Color(0xFFD95D9E)
        PlayerColour.Indigo -> Color(0xFF5367C9)
        PlayerColour.Lime -> Color(0xFF82A83D)
    }

internal fun PlayerColour.toOnUiColor(): Color =
    when (this) {
        PlayerColour.Yellow, PlayerColour.Orange, PlayerColour.Teal, PlayerColour.Lime -> MidnightPaperColors.ink
        PlayerColour.Blue, PlayerColour.Green, PlayerColour.Red, PlayerColour.Purple, PlayerColour.Pink, PlayerColour.Indigo ->
            MidnightPaperColors.paperWhite
    }
