package com.example.storyround.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalStoryRoundColors =
    staticCompositionLocalOf<StoryRoundColors> {
        error("StoryRoundColors are only available inside StoryRoundTheme.")
    }

private val LocalStoryRoundTypography =
    staticCompositionLocalOf<StoryRoundTypography> {
        error("StoryRoundTypography is only available inside StoryRoundTheme.")
    }

internal object StoryRoundTheme {
    val colors: StoryRoundColors
        @Composable
        @ReadOnlyComposable
        get() = LocalStoryRoundColors.current

    val typography: StoryRoundTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalStoryRoundTypography.current
}

@Composable
internal fun StoryRoundTheme(content: @Composable () -> Unit) {
    val fonts = storyRoundFonts()
    val typography = remember(fonts) { storyRoundTypography(fonts) }
    CompositionLocalProvider(
        LocalStoryRoundColors provides DarkColors,
        LocalStoryRoundTypography provides typography,
    ) {
        MaterialTheme(
            colorScheme = DarkColors.toMaterialColorScheme(),
            content = content,
        )
    }
}

private fun StoryRoundColors.toMaterialColorScheme(): ColorScheme =
    darkColorScheme(
        primary = ink,
        onPrimary = paperWhite,
        background = stage,
        onBackground = onStage,
        surface = stage,
        onSurface = onStage,
    )
