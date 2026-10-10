package com.example.storyround.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
internal data class StoryRoundColors(
    val stage: Color,
    val stageRaised: Color,
    val stageRays: Color,
    val stageOutline: Color,
    val stageOutlineSoft: Color,
    val stageHairline: Color,
    val stageDivider: Color,
    val stageScrim: Color,
    val stageDisabledFill: Color,
    val stageDisabledOutline: Color,
    val onStage: Color,
    val onStageSoft: Color,
    val onStageMuted: Color,
    val onStageDisabled: Color,
    val paper: Color,
    val paperWhite: Color,
    val onPaperMuted: Color,
    val ink: Color,
    val inkShadow: Color,
    val disabledFill: Color,
    val cue: Color,
    val marigold: Color,
    val coral: Color,
)

internal val MidnightPaperColors =
    StoryRoundColors(
        stage = Color(0xFF231A45),
        stageRaised = Color(0xFF2F2560),
        stageRays = Color(0x0BFFFFFF),
        stageOutline = Color(0x4DF4EEFF),
        stageOutlineSoft = Color(0x47F4EEFF),
        stageHairline = Color(0x1FF4EEFF),
        stageDivider = Color(0x1AF4EEFF),
        stageScrim = Color(0x5917122B),
        stageDisabledFill = Color(0x0FF4EEFF),
        stageDisabledOutline = Color(0x26F4EEFF),
        onStage = Color(0xFFF4EEFF),
        onStageSoft = Color(0xFFD9D1F2),
        onStageMuted = Color(0xFFB7AEDB),
        onStageDisabled = Color(0xFF7F76A6),
        paper = Color(0xFFFFF7E8),
        paperWhite = Color(0xFFFFFFFF),
        onPaperMuted = Color(0xFF6B6380),
        ink = Color(0xFF17122B),
        inkShadow = Color(0xFF0D0A1C),
        disabledFill = Color(0xFFE9E1CF),
        cue = Color(0xFF46D4F2),
        marigold = Color(0xFFFFC53D),
        coral = Color(0xFFFF6B5A),
    )
