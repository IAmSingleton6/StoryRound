package com.example.storyround.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.ui.theme.StoryRoundTheme
import com.example.storyround.ui.theme.toOnUiColor
import com.example.storyround.ui.theme.toUiColor

private const val INITIAL_TO_SIZE_RATIO = 0.46f

@Composable
internal fun PlayerAvatar(
    player: PlayerUi,
    size: Dp,
    modifier: Modifier = Modifier,
    borderWidth: Dp = 2.dp,
    initialSize: TextUnit = TextUnit.Unspecified,
) {
    val scaledSize = with(LocalDensity.current) { (size * INITIAL_TO_SIZE_RATIO).toSp() }
    val fontSize = if (initialSize.isSpecified) initialSize else scaledSize
    Box(
        modifier =
            modifier
                .size(size)
                .background(player.colour.toUiColor(), CircleShape)
                .border(borderWidth, StoryRoundTheme.colors.ink, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = player.initial,
            style = StoryRoundTheme.typography.avatarInitial.copy(color = player.colour.toOnUiColor(), fontSize = fontSize),
        )
    }
}

internal val PlayerUi.initial: String
    get() = name.trim().firstOrNull()?.uppercase() ?: "?"
