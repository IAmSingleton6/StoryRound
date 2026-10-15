package com.example.storyround.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.preview.ComponentPreview
import com.example.storyround.ui.theme.StoryRoundTheme
import org.jetbrains.compose.resources.stringResource
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.host

private const val CROWN_PATH = "M3 8l4.5 4L12 5l4.5 7L21 8l-2 11H5z"
private const val CROWN_STROKE_WIDTH = 1.6f

@Composable
internal fun HostCrown(
    fill: Color,
    outline: Color,
    modifier: Modifier = Modifier,
) {
    val crown = remember(fill, outline) { crown(fill, outline) }
    Image(painter = rememberVectorPainter(crown), contentDescription = stringResource(Res.string.host), modifier = modifier)
}

private fun crown(
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

@Preview
@Composable
private fun HostCrownPreview() =
    ComponentPreview {
        val colors = StoryRoundTheme.colors
        HostCrown(fill = colors.marigold, outline = colors.ink, modifier = Modifier.size(32.dp))
    }
