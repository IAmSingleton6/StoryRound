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
import androidx.compose.ui.unit.dp
import com.example.storyround.presentation.model.StoryWriterUi
import com.example.storyround.ui.theme.StoryRoundTheme
import com.example.storyround.ui.theme.toUiColor

@Composable
internal fun WriterShareStrip(
    writers: List<StoryWriterUi>,
    modifier: Modifier = Modifier,
) {
    val ink = StoryRoundTheme.colors.ink
    Row(
        modifier
            .fillMaxWidth()
            .height(14.dp)
            .drawBehind { drawRect(ink, topLeft = Offset(0f, size.height - 2.dp.toPx())) },
    ) {
        writers.filter { it.pieces > 0 }.forEach { writer ->
            Box(
                Modifier
                    .weight(writer.pieces.toFloat())
                    .height(12.dp)
                    .background(writer.player.colour.toUiColor()),
            )
        }
    }
}
