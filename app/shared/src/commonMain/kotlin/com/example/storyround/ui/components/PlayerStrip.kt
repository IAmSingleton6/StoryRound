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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.ui.preview.ComponentPreview
import com.example.storyround.ui.preview.PreviewPlayers
import com.example.storyround.ui.theme.StoryRoundTheme
import com.example.storyround.ui.theme.toColor
import org.jetbrains.compose.resources.stringResource
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.player_writing_now

private const val STRIP_ANIMATION_MILLIS = 300
private const val WAITING_ALPHA = 0.55f
private val ActiveAvatarSize = 36.dp
private val WaitingAvatarSize = 28.dp
private val StripInitialSize = 13.sp

private val ActiveRingReach = 5.dp
private val ActiveRingGap = 3.dp

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
    val size by animateDpAsState(if (isActive) ActiveAvatarSize else WaitingAvatarSize, tween(STRIP_ANIMATION_MILLIS), label = "stripSize")
    val alpha by animateFloatAsState(if (isActive) 1f else WAITING_ALPHA, tween(STRIP_ANIMATION_MILLIS), label = "stripAlpha")
    val stage = StoryRoundTheme.colors.stage
    val description = if (isActive) stringResource(Res.string.player_writing_now, player.name) else player.name
    PlayerAvatar(
        player = player,
        size = size,
        initialSize = StripInitialSize,
        modifier =
            Modifier
                .semantics { contentDescription = description }
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
        drawCircle(ring, radius = radius + ActiveRingReach.toPx())
        drawCircle(gap, radius = radius + ActiveRingGap.toPx())
    }

@Preview
@Composable
private fun PlayerStripPreview() =
    ComponentPreview {
        PlayerStrip(PreviewPlayers.all, activePlayerId = PreviewPlayers.alex.id)
        PlayerStrip(PreviewPlayers.all, activePlayerId = null)
    }
