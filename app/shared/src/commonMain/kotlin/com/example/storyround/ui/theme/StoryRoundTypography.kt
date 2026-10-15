package com.example.storyround.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Immutable
internal data class StoryRoundTypography(
    val displayLarge: TextStyle,
    val displayMedium: TextStyle,
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val bodyXSmall: TextStyle,
    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle,
    val labelXSmall: TextStyle,
    val eyebrowLarge: TextStyle,
    val eyebrowMedium: TextStyle,
    val eyebrowSmall: TextStyle,
    val tag: TextStyle,
    val buttonLarge: TextStyle,
    val buttonSmall: TextStyle,
    val link: TextStyle,
    val input: TextStyle,
    val codeInput: TextStyle,
    val avatarInitial: TextStyle,
    val roomCode: TextStyle,
    val countdown: TextStyle,
    val numberLarge: TextStyle,
    val numberSmall: TextStyle,
    val counter: TextStyle,
    val story: TextStyle,
    val storyExcerpt: TextStyle,
    val storyInput: TextStyle,
    val storyPlaceholder: TextStyle,
)

internal fun storyRoundTypography(fonts: StoryRoundFonts): StoryRoundTypography {
    val display = TextStyle(fontFamily = fonts.display, fontWeight = FontWeight.ExtraBold)
    val ui = TextStyle(fontFamily = fonts.ui)
    val mono = TextStyle(fontFamily = fonts.mono, fontWeight = FontWeight.Bold)
    val serif = TextStyle(fontFamily = fonts.serif, fontWeight = FontWeight.Normal)
    return StoryRoundTypography(
        displayLarge =
            display.copy(
                fontSize = 50.sp,
                letterSpacing = (-0.03).em,
                lineHeight = 0.92.em,
                lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both),
            ),
        displayMedium = display.copy(fontSize = 40.sp, letterSpacing = (-0.02).em, lineHeight = 1.1.em),
        headlineLarge = ui.copy(fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, letterSpacing = (-0.01).em, lineHeight = 1.1.em),
        headlineMedium = ui.copy(fontWeight = FontWeight.ExtraBold, fontSize = 24.sp),
        titleLarge = ui.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, letterSpacing = 0.02.em),
        titleMedium = ui.copy(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, lineHeight = 1.3.em),
        bodyLarge = ui.copy(fontWeight = FontWeight.Medium, fontSize = 17.sp, lineHeight = 1.45.em),
        bodyMedium = ui.copy(fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 1.5.em),
        bodySmall = ui.copy(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 1.4.em),
        bodyXSmall = ui.copy(fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 1.4.em),
        labelLarge = ui.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
        labelMedium = ui.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
        labelSmall = ui.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 1.4.em),
        labelXSmall = ui.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
        eyebrowLarge = ui.copy(fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, letterSpacing = 0.12.em),
        eyebrowMedium = ui.copy(fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, letterSpacing = 0.14.em),
        eyebrowSmall = ui.copy(fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, letterSpacing = 0.14.em),
        tag = ui.copy(fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 0.06.em),
        buttonLarge = ui.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, letterSpacing = 0.06.em),
        buttonSmall = ui.copy(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 0.06.em),
        link = ui.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp, textDecoration = TextDecoration.Underline),
        input = ui.copy(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
        codeInput = mono.copy(fontSize = 28.sp, letterSpacing = 0.35.em),
        avatarInitial = ui.copy(fontWeight = FontWeight.ExtraBold),
        roomCode = mono.copy(fontSize = 44.sp, letterSpacing = 0.12.em, lineHeight = 1.1.em),
        countdown = mono.copy(fontSize = 36.sp, lineHeight = 1.em, fontFeatureSettings = "tnum"),
        numberLarge = mono.copy(fontSize = 28.sp),
        numberSmall = mono.copy(fontSize = 20.sp, lineHeight = 1.em),
        counter = mono.copy(fontWeight = FontWeight.Medium, fontSize = 13.sp),
        story = serif.copy(fontSize = 18.sp, lineHeight = 1.75.em),
        storyExcerpt = serif.copy(fontSize = 18.sp, lineHeight = 1.45.em),
        storyInput = serif.copy(fontSize = 17.sp, lineHeight = 1.5.em),
        storyPlaceholder = serif.copy(fontSize = 22.sp, fontStyle = FontStyle.Italic),
    )
}
