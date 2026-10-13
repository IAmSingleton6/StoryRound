package com.example.storyround.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.storyround.protocol.PlayerColour

internal fun PlayerColour.toColor(): Color =
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

internal fun PlayerColour.toContentColor(): Color =
    when (this) {
        PlayerColour.Yellow, PlayerColour.Orange, PlayerColour.Teal, PlayerColour.Lime -> MidnightPaperColors.ink
        PlayerColour.Blue, PlayerColour.Green, PlayerColour.Red, PlayerColour.Purple, PlayerColour.Pink, PlayerColour.Indigo ->
            MidnightPaperColors.paperWhite
    }
