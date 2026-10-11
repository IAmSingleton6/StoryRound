package com.example.storyround.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.storyround.presentation.model.StoryWriterUi
import com.example.storyround.ui.theme.StoryRoundTheme

private val WriterChipShape = RoundedCornerShape(16.dp)

@Composable
internal fun WriterLegend(
    writers: List<StoryWriterUi>,
    modifier: Modifier = Modifier,
    currentPlayerId: String? = null,
) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BasicText("PLAYERS", style = typography.eyebrowSmall.copy(color = colors.onStageMuted))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            writers.forEach { writer ->
                val name = if (writer.player.id == currentPlayerId) "${writer.player.name} (you)" else writer.player.name
                Row(
                    modifier =
                        Modifier
                            .height(32.dp)
                            .background(colors.stageRaised, WriterChipShape)
                            .semantics(mergeDescendants = true) { contentDescription = "$name, ${writer.pieces} pieces" }
                            .padding(start = 4.dp, end = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlayerAvatar(writer.player, size = 24.dp, borderWidth = 1.5.dp, initialSize = 11.sp)
                    BasicText(name, style = typography.callout.copy(color = colors.onStage))
                    BasicText("×${writer.pieces}", style = typography.counterSmall.copy(color = colors.onStageMuted))
                }
            }
        }
    }
}
