package com.example.storyround.ui.components

import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.storyround.ui.theme.StoryRoundTheme

private const val RAY_PERIOD_DEGREES = 20f
private const val RAY_WIDTH_DEGREES = 8f
private const val RAYS_CENTRE_Y_FRACTION = 0.2f
private const val NANOS_PER_MILLI = 1_000_000L

private const val RAY_PERIOD_MILLIS = 6_000L

@Composable
internal fun StageScaffold(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = StoryRoundTheme.colors
    val rotation = rememberRayRotation()
    Box(
        modifier = modifier.fillMaxSize().stageRays(colors.stage, colors.stageRays, rotation::floatValue),
        content = content,
    )
}

@Composable
private fun rememberRayRotation(): FloatState {
    val rotation = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(rotation) {
        while (true) {
            withInfiniteAnimationFrameNanos { frameNanos ->
                val periodProgress = (frameNanos / NANOS_PER_MILLI % RAY_PERIOD_MILLIS).toFloat() / RAY_PERIOD_MILLIS
                rotation.floatValue = periodProgress * RAY_PERIOD_DEGREES
            }
        }
    }
    return rotation
}

private fun Modifier.stageRays(
    stage: Color,
    ray: Color,
    rotation: () -> Float,
): Modifier =
    drawWithCache {
        val centre = Offset(size.width / 2f, size.height * RAYS_CENTRE_Y_FRACTION)
        val rays = Brush.sweepGradient(*rayColorStops(ray), center = centre)
        val reach = Offset(maxOf(centre.x, size.width - centre.x), maxOf(centre.y, size.height - centre.y)).getDistance()
        onDrawBehind {
            drawRect(stage)
            rotate(rotation(), pivot = centre) { drawCircle(rays, radius = reach, center = centre) }
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
