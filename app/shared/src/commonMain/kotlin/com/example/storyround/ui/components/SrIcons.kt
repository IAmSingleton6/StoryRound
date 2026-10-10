package com.example.storyround.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp

internal object SrIcons {
    val Book: ImageVector by lazy {
        strokeIcon(
            name = "Book",
            strokeWidth = 2.4f,
            paths =
                listOf(
                    "M4 19.5A2.5 2.5 0 0 1 6.5 17H20",
                    "M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z",
                ),
        )
    }

    val Alert: ImageVector by lazy {
        strokeIcon(
            name = "Alert",
            strokeWidth = 2.6f,
            paths = listOf("M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0z", "M12 7v6M12 16.5v.5"),
        )
    }

    private fun strokeIcon(
        name: String,
        strokeWidth: Float,
        paths: List<String>,
    ): ImageVector {
        val builder =
            ImageVector.Builder(
                name = name,
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
        paths.forEach { path ->
            builder.addPath(
                pathData = addPathNodes(path),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = strokeWidth,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
        return builder.build()
    }
}

@Composable
internal fun SrIcon(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = rememberVectorPainter(icon),
        contentDescription = null,
        modifier = modifier,
        colorFilter = ColorFilter.tint(tint),
    )
}
