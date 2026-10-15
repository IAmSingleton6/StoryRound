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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.storyround.presentation.model.StoryWriterUi
import com.example.storyround.presentation.model.legendNameFor
import com.example.storyround.ui.preview.ComponentPreview
import com.example.storyround.ui.preview.PreviewPlayers
import com.example.storyround.ui.preview.PreviewStory
import com.example.storyround.ui.theme.OutlineWidth
import com.example.storyround.ui.theme.StoryRoundTheme
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.count_pieces
import storyround.app.shared.generated.resources.writer_legend_chip_description
import storyround.app.shared.generated.resources.writer_legend_title

private val WriterChipShape = RoundedCornerShape(16.dp)
private val WriterChipHeight = 32.dp

@Composable
internal fun WriterLegend(
    writers: List<StoryWriterUi>,
    modifier: Modifier = Modifier,
    currentPlayerId: String? = null,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BasicText(
            stringResource(Res.string.writer_legend_title),
            style = StoryRoundTheme.typography.eyebrowSmall.copy(color = StoryRoundTheme.colors.onStageMuted),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            writers.forEach { writer -> WriterChip(writer, name = writer.player.legendNameFor(currentPlayerId)) }
        }
    }
}

@Composable
private fun WriterChip(
    writer: StoryWriterUi,
    name: String,
) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    val pieces = pluralStringResource(Res.plurals.count_pieces, writer.pieces, writer.pieces)
    val description = stringResource(Res.string.writer_legend_chip_description, name, pieces)
    Row(
        modifier =
            Modifier
                .height(WriterChipHeight)
                .background(colors.stageRaised, WriterChipShape)
                .semantics(mergeDescendants = true) { contentDescription = description }
                .padding(start = 4.dp, end = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayerAvatar(writer.player, size = 24.dp, borderWidth = OutlineWidth.Fine, initialSize = 11.sp)
        BasicText(name, style = typography.labelSmall.copy(color = colors.onStage))
        BasicText("×${writer.pieces}", style = typography.counter.copy(color = colors.onStageMuted))
    }
}

@Preview
@Composable
private fun WriterLegendPreview() = ComponentPreview { WriterLegend(PreviewStory.writers, currentPlayerId = PreviewPlayers.alex.id) }
