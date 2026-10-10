package com.example.storyround.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal fun Modifier.hardShadow(
    offsetY: Dp,
    color: Color,
    shape: Shape,
): Modifier =
    drawBehind {
        val outline = shape.createOutline(size, layoutDirection, this)
        translate(top = offsetY.toPx()) { drawOutline(outline, color) }
    }

internal fun Modifier.paperSheet(
    paper: Color,
    edge: Color,
): Modifier =
    drawWithCache {
        val radius = 28.dp.toPx()
        val edgeWidth = 3.dp.toPx()
        val outer = CornerRadius(radius)
        val inner = CornerRadius(radius, radius - edgeWidth)
        val edgePath = Path()
        edgePath.addRoundRect(
            RoundRect(0f, 0f, size.width, size.height, topLeftCornerRadius = outer, topRightCornerRadius = outer),
        )
        val paperPath = Path()
        paperPath.addRoundRect(
            RoundRect(
                left = 0f,
                top = edgeWidth,
                right = size.width,
                bottom = size.height,
                topLeftCornerRadius = inner,
                topRightCornerRadius = inner,
            ),
        )
        onDrawBehind {
            drawPath(edgePath, edge)
            drawPath(paperPath, paper)
        }
    }

internal fun Modifier.tilt(
    degrees: Float,
    dx: Dp,
    dy: Dp,
): Modifier =
    graphicsLayer {
        val radians = degrees * PI / 180.0
        val x = dx.toPx()
        val y = dy.toPx()
        rotationZ = degrees
        translationX = (x * cos(radians) - y * sin(radians)).toFloat()
        translationY = (x * sin(radians) + y * cos(radians)).toFloat()
    }
