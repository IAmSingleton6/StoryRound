package com.example.storyround.ui.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal enum class SrButtonSize(
    val height: Dp,
    val cornerRadius: Dp,
    val shadow: Dp,
) {
    Large(height = 60.dp, cornerRadius = 16.dp, shadow = 5.dp),
    Compact(height = 48.dp, cornerRadius = 14.dp, shadow = 4.dp),
}
