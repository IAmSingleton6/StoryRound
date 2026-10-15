package com.example.storyround.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.storyround.presentation.model.StoryWriterUi
import com.example.storyround.ui.preview.ComponentPreview
import com.example.storyround.ui.preview.PreviewStory
import com.example.storyround.ui.preview.PreviewSurface
import com.example.storyround.ui.theme.OutlineWidth
import com.example.storyround.ui.theme.StoryRoundTheme
import com.example.storyround.ui.theme.toColor

private val ShareBarHeight = 12.dp

@Composable
internal fun WriterShareStrip(
    writers: List<StoryWriterUi>,
    modifier: Modifier = Modifier,
) {
    val ink = StoryRoundTheme.colors.ink
    Row(
        modifier
            .fillMaxWidth()
            .height(ShareBarHeight + OutlineWidth.Thin)
            .drawBehind { drawRect(ink, topLeft = Offset(0f, size.height - OutlineWidth.Thin.toPx())) },
    ) {
        writers.filter { it.pieces > 0 }.forEach { writer ->
            Box(
                Modifier
                    .weight(writer.pieces.toFloat())
                    .height(ShareBarHeight)
                    .background(writer.player.colour.toColor()),
            )
        }
    }
}

@Preview
@Composable
private fun WriterShareStripPreview() = ComponentPreview(PreviewSurface.Paper) { WriterShareStrip(PreviewStory.writers) }
