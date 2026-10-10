package com.example.storyround.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.unit.Dp

private const val DASH_TO_WIDTH_RATIO = 3f
private const val GAP_TO_WIDTH_RATIO = 2.5f

internal fun Modifier.dashedBorder(
    width: Dp,
    color: Color,
    shape: Shape,
): Modifier =
    drawWithCache {
        val strokeWidth = width.toPx()
        val half = strokeWidth / 2f
        val insetSize = size.copy(width = size.width - strokeWidth, height = size.height - strokeWidth)
        val outline = shape.createOutline(insetSize, layoutDirection, this)
        val stroke =
            Stroke(
                width = strokeWidth,
                pathEffect =
                    PathEffect.dashPathEffect(
                        floatArrayOf(strokeWidth * DASH_TO_WIDTH_RATIO, strokeWidth * GAP_TO_WIDTH_RATIO),
                    ),
            )
        onDrawWithContent {
            drawContent()
            inset(half, half) { drawOutline(outline, color, style = stroke) }
        }
    }
