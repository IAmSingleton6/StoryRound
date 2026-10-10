package com.example.storyround.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.storyround.ui.theme.StoryRoundTheme

private const val RAY_PERIOD_DEGREES = 20f
private const val RAY_WIDTH_DEGREES = 8f
private const val RAYS_CENTRE_Y_FRACTION = 0.2f

@Composable
internal fun StageScaffold(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = StoryRoundTheme.colors
    Box(
        modifier = modifier.fillMaxSize().stageRays(colors.stage, colors.stageRays),
        content = content,
    )
}

private fun Modifier.stageRays(
    stage: Color,
    ray: Color,
): Modifier =
    drawWithCache {
        val rays =
            Brush.sweepGradient(
                *rayColorStops(ray),
                center = Offset(size.width / 2f, size.height * RAYS_CENTRE_Y_FRACTION),
            )
        onDrawBehind {
            drawRect(stage)
            drawRect(rays)
        }
    }

private fun rayColorStops(ray: Color): Array<Pair<Float, Color>> {
    val rays = (360f / RAY_PERIOD_DEGREES).toInt()
    val phase = RAY_PERIOD_DEGREES / 2f
    return buildList {
        add(0f to Color.Transparent)
        repeat(rays) { index ->
            val start = (index * RAY_PERIOD_DEGREES + phase) / 360f
            val end = (index * RAY_PERIOD_DEGREES + phase + RAY_WIDTH_DEGREES) / 360f
            add(start to Color.Transparent)
            add(start to ray)
            add(end to ray)
            add(end to Color.Transparent)
        }
        add(1f to Color.Transparent)
    }.toTypedArray()
}
