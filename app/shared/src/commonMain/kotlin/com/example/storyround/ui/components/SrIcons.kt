package com.example.storyround.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

private const val CROWN_PATH = "M3 8l4.5 4L12 5l4.5 7L21 8l-2 11H5z"
private const val CROWN_STROKE_WIDTH = 1.6f

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

    val Back: ImageVector by lazy {
        strokeIcon(name = "Back", strokeWidth = 2.6f, paths = listOf("M19 12H5M12 19l-7-7 7-7"))
    }

    val Dice: ImageVector by lazy {
        strokeIcon(
            name = "Dice",
            strokeWidth = 2.2f,
            paths = listOf("M7.5 3.5h9a4 4 0 0 1 4 4v9a4 4 0 0 1 -4 4h-9a4 4 0 0 1 -4 -4v-9a4 4 0 0 1 4 -4z"),
            filledPaths =
                listOf(
                    "M7.1 8.5a1.4 1.4 0 1 0 2.8 0a1.4 1.4 0 1 0 -2.8 0z",
                    "M10.6 12a1.4 1.4 0 1 0 2.8 0a1.4 1.4 0 1 0 -2.8 0z",
                    "M14.1 15.5a1.4 1.4 0 1 0 2.8 0a1.4 1.4 0 1 0 -2.8 0z",
                ),
        )
    }

    val Feather: ImageVector by lazy {
        strokeIcon(
            name = "Feather",
            strokeWidth = 2.2f,
            paths = listOf("M20.2 12.2a6 6 0 0 0-8.5-8.5L5 10.5V19h8.5z", "M16 8L2 22M17.5 15H9"),
        )
    }

    val Send: ImageVector by lazy {
        strokeIcon(name = "Send", strokeWidth = 2.8f, paths = listOf("M5 12h14M13 6l6 6-6 6"))
    }

    val Replay: ImageVector by lazy {
        strokeIcon(name = "Replay", strokeWidth = 2.6f, paths = listOf("M3 12a9 9 0 1 0 3-6.7L3 8", "M3 3v5h5"))
    }

    val Minus: ImageVector by lazy {
        strokeIcon(name = "Minus", strokeWidth = 3f, paths = listOf("M5 12h14"))
    }

    val Plus: ImageVector by lazy {
        strokeIcon(name = "Plus", strokeWidth = 3f, paths = listOf("M12 5v14M5 12h14"))
    }

    val WifiOff: ImageVector by lazy {
        strokeIcon(
            name = "WifiOff",
            strokeWidth = 2.6f,
            paths =
                listOf(
                    "M2 2l20 20M8.5 16.4a5 5 0 0 1 7 0M5 12.6a10 10 0 0 1 5.2-2.8M19 12.6a10 10 0 0 0-2.3-1.6M12 20h.01",
                ),
        )
    }

    fun crown(
        fill: Color,
        outline: Color,
    ): ImageVector =
        ImageVector
            .Builder(
                name = "Crown",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).addPath(
                pathData = addPathNodes(CROWN_PATH),
                fill = SolidColor(fill),
                stroke = SolidColor(outline),
                strokeLineWidth = CROWN_STROKE_WIDTH,
                strokeLineJoin = StrokeJoin.Round,
            ).build()

    private fun strokeIcon(
        name: String,
        strokeWidth: Float,
        paths: List<String>,
        filledPaths: List<String> = emptyList(),
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
        filledPaths.forEach { path ->
            builder.addPath(pathData = addPathNodes(path), fill = SolidColor(Color.Black))
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

@Composable
internal fun HostCrown(
    fill: Color,
    outline: Color,
    modifier: Modifier = Modifier,
) {
    val crown = remember(fill, outline) { SrIcons.crown(fill, outline) }
    Image(painter = rememberVectorPainter(crown), contentDescription = "Host", modifier = modifier)
}
