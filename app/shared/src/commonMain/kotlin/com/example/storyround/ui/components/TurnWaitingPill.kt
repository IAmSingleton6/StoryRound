package com.example.storyround.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.ui.theme.StoryRoundTheme
import com.example.storyround.ui.theme.toUiColor

private val WaitingPillShape = RoundedCornerShape(30.dp)
private const val DOT_COUNT = 3
private const val DOT_STEP_MILLIS = 330
private const val DOT_IDLE_ALPHA = 0.35f

@Composable
internal fun TurnWaitingPill(
    writer: PlayerUi,
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = StoryRoundTheme.colors
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(colors.stageRaised, WaitingPillShape)
                .dashedBorder(2.dp, colors.stageOutlineSoft, WaitingPillShape)
                .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }
                .padding(start = 10.dp, end = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayerAvatar(writer, size = 38.dp)
        BasicText(text, modifier = Modifier.weight(1f), style = StoryRoundTheme.typography.bodyStrong.copy(color = colors.onStage))
        WritingDots(writer)
    }
}

@Composable
private fun WritingDots(writer: PlayerUi) {
    val transition = rememberInfiniteTransition(label = "writingDots")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = DOT_COUNT.toFloat(),
        animationSpec = infiniteRepeatable(tween(DOT_STEP_MILLIS * DOT_COUNT, easing = LinearEasing), RepeatMode.Restart),
        label = "writingDotsPhase",
    )
    val active = phase.toInt().coerceAtMost(DOT_COUNT - 1)
    Row(Modifier.clearAndSetSemantics {}, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        repeat(DOT_COUNT) { index ->
            Box(
                Modifier
                    .size(7.dp)
                    .alpha(if (index == active) 1f else DOT_IDLE_ALPHA)
                    .background(writer.colour.toUiColor(), CircleShape),
            )
        }
    }
}
