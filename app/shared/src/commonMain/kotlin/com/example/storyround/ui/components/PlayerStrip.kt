package com.example.storyround.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.ui.theme.StoryRoundTheme
import com.example.storyround.ui.theme.toColor

private const val STRIP_ANIMATION_MILLIS = 300
private const val WAITING_ALPHA = 0.55f

@Composable
internal fun PlayerStrip(
    players: List<PlayerUi>,
    activePlayerId: String?,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
        players.forEach { player -> StripAvatar(player, isActive = player.id == activePlayerId) }
    }
}

@Composable
private fun StripAvatar(
    player: PlayerUi,
    isActive: Boolean,
) {
    val animation = tween<Float>(STRIP_ANIMATION_MILLIS)
    val size by animateDpAsState(if (isActive) 36.dp else 28.dp, tween(STRIP_ANIMATION_MILLIS), label = "stripSize")
    val alpha by animateFloatAsState(if (isActive) 1f else WAITING_ALPHA, animation, label = "stripAlpha")
    val stage = StoryRoundTheme.colors.stage
    PlayerAvatar(
        player = player,
        size = size,
        initialSize = 13.sp,
        modifier =
            Modifier
                .semantics { contentDescription = if (isActive) "${player.name}, writing now" else player.name }
                .then(if (isActive) Modifier.activeRing(player.colour.toColor(), stage) else Modifier)
                .alpha(alpha),
    )
}

private fun Modifier.activeRing(
    ring: Color,
    gap: Color,
): Modifier =
    drawBehind {
        val radius = size.minDimension / 2f
        drawCircle(ring, radius = radius + 5.dp.toPx())
        drawCircle(gap, radius = radius + 3.dp.toPx())
    }
