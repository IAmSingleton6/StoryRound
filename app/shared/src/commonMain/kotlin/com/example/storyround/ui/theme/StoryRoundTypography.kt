package com.example.storyround.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.bricolage_grotesque_bold
import storyround.app.shared.generated.resources.bricolage_grotesque_display_extrabold
import storyround.app.shared.generated.resources.bricolage_grotesque_extrabold
import storyround.app.shared.generated.resources.bricolage_grotesque_medium
import storyround.app.shared.generated.resources.bricolage_grotesque_semibold
import storyround.app.shared.generated.resources.jetbrains_mono_bold
import storyround.app.shared.generated.resources.jetbrains_mono_medium

@Immutable
internal data class StoryRoundFonts(
    val ui: FontFamily,
    val display: FontFamily,
    val mono: FontFamily,
)

@Immutable
internal data class StoryRoundTypography(
    val display: TextStyle,
    val lead: TextStyle,
    val label: TextStyle,
    val input: TextStyle,
    val codeInput: TextStyle,
    val button: TextStyle,
    val pill: TextStyle,
    val badge: TextStyle,
    val callout: TextStyle,
    val link: TextStyle,
    val eyebrow: TextStyle,
    val eyebrowSmall: TextStyle,
    val title: TextStyle,
    val bodyStrong: TextStyle,
    val caption: TextStyle,
    val captionStrong: TextStyle,
    val meta: TextStyle,
    val metaStrong: TextStyle,
    val tag: TextStyle,
    val micro: TextStyle,
    val avatarInitial: TextStyle,
    val roomCode: TextStyle,
    val numberLarge: TextStyle,
    val numberSmall: TextStyle,
)

@Composable
internal fun storyRoundFonts(): StoryRoundFonts =
    StoryRoundFonts(
        ui =
            FontFamily(
                Font(Res.font.bricolage_grotesque_medium, FontWeight.Medium),
                Font(Res.font.bricolage_grotesque_semibold, FontWeight.SemiBold),
                Font(Res.font.bricolage_grotesque_bold, FontWeight.Bold),
                Font(Res.font.bricolage_grotesque_extrabold, FontWeight.ExtraBold),
            ),
        display = FontFamily(Font(Res.font.bricolage_grotesque_display_extrabold, FontWeight.ExtraBold)),
        mono =
            FontFamily(
                Font(Res.font.jetbrains_mono_medium, FontWeight.Medium),
                Font(Res.font.jetbrains_mono_bold, FontWeight.Bold),
            ),
    )

internal fun storyRoundTypography(fonts: StoryRoundFonts): StoryRoundTypography {
    val ui = TextStyle(fontFamily = fonts.ui)
    val mono = TextStyle(fontFamily = fonts.mono, fontWeight = FontWeight.Bold)
    return StoryRoundTypography(
        display =
            TextStyle(
                fontFamily = fonts.display,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 50.sp,
                letterSpacing = (-0.03).em,
                lineHeight = 0.92.em,
                lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both),
            ),
        lead = ui.copy(fontWeight = FontWeight.Medium, fontSize = 17.sp, lineHeight = 1.45.em),
        label = ui.copy(fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, letterSpacing = 0.1.em),
        input = ui.copy(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
        codeInput =
            TextStyle(
                fontFamily = fonts.mono,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                letterSpacing = 0.35.em,
            ),
        button = ui.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, letterSpacing = 0.06.em),
        pill = ui.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
        badge = ui.copy(fontWeight = FontWeight.ExtraBold, fontSize = 13.sp),
        callout = ui.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 1.4.em),
        link = ui.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp, textDecoration = TextDecoration.Underline),
        eyebrow = ui.copy(fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, letterSpacing = 0.14.em),
        eyebrowSmall = ui.copy(fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, letterSpacing = 0.14.em),
        title = ui.copy(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, lineHeight = 1.3.em),
        bodyStrong = ui.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
        caption = ui.copy(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 1.4.em),
        captionStrong = ui.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
        meta = ui.copy(fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 1.4.em),
        metaStrong = ui.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
        tag = ui.copy(fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 0.06.em),
        micro = ui.copy(fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, letterSpacing = 0.08.em, lineHeight = 1.em),
        avatarInitial = ui.copy(fontWeight = FontWeight.ExtraBold),
        roomCode = mono.copy(fontSize = 44.sp, letterSpacing = 0.12.em, lineHeight = 1.1.em),
        numberLarge = mono.copy(fontSize = 28.sp),
        numberSmall = mono.copy(fontSize = 20.sp, lineHeight = 1.em),
    )
}
