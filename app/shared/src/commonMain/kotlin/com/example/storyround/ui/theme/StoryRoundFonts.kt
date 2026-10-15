package com.example.storyround.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.bricolage_grotesque_bold
import storyround.app.shared.generated.resources.bricolage_grotesque_display_extrabold
import storyround.app.shared.generated.resources.bricolage_grotesque_extrabold
import storyround.app.shared.generated.resources.bricolage_grotesque_medium
import storyround.app.shared.generated.resources.bricolage_grotesque_semibold
import storyround.app.shared.generated.resources.jetbrains_mono_bold
import storyround.app.shared.generated.resources.jetbrains_mono_medium
import storyround.app.shared.generated.resources.literata
import storyround.app.shared.generated.resources.literata_italic

private const val LITERATA_OPTICAL_SIZE = 18f

@Immutable
internal data class StoryRoundFonts(
    val ui: FontFamily,
    val display: FontFamily,
    val mono: FontFamily,
    val serif: FontFamily,
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
        serif =
            FontFamily(
                literata(FontWeight.Normal, FontStyle.Normal),
                literata(FontWeight.SemiBold, FontStyle.Normal),
                literata(FontWeight.Normal, FontStyle.Italic),
            ),
    )

@Composable
private fun literata(
    weight: FontWeight,
    style: FontStyle,
) = Font(
    resource = if (style == FontStyle.Italic) Res.font.literata_italic else Res.font.literata,
    weight = weight,
    style = style,
    variationSettings =
        FontVariation.Settings(
            FontVariation.weight(weight.weight),
            FontVariation.Setting("opsz", LITERATA_OPTICAL_SIZE),
        ),
)
