package com.example.storyround.presentation.finished

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.ui.components.FlatButton
import com.example.storyround.ui.components.RaisedButton
import com.example.storyround.ui.components.RoomScreenPadding
import com.example.storyround.ui.components.StageScreen
import com.example.storyround.ui.components.StoryPaper
import com.example.storyround.ui.components.StoryText
import com.example.storyround.ui.components.WriterLegend
import com.example.storyround.ui.components.printedSurface
import com.example.storyround.ui.components.tilt
import com.example.storyround.ui.preview.PhonePreview
import com.example.storyround.ui.preview.PreviewPlayers
import com.example.storyround.ui.preview.ScreenPreview
import com.example.storyround.ui.theme.StoryRoundTheme
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.count_contributions
import storyround.app.shared.generated.resources.count_writers
import storyround.app.shared.generated.resources.finished_home
import storyround.app.shared.generated.resources.finished_play_again
import storyround.app.shared.generated.resources.finished_play_again_hint
import storyround.app.shared.generated.resources.finished_story_complete
import storyround.app.shared.generated.resources.finished_summary
import storyround.app.shared.generated.resources.finished_the_end
import storyround.app.shared.generated.resources.ic_replay

private const val STAMP_TILT_DEGREES = -3f
private val StampShape = RoundedCornerShape(10.dp)
private val FinishedStoryPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 24.dp)

@Composable
internal fun FinishedScreen(
    uiState: FinishedUiState,
    onAction: (FinishedAction) -> Unit,
) = StageScreen(
    contentPadding = RoomScreenPadding,
    isReconnecting = uiState.isReconnecting,
    verticalArrangement = Arrangement.spacedBy(14.dp),
) {
    FinishedHeader(summary = finishedSummary(uiState.contributionCount, uiState.writers.size))
    StoryPaper(Modifier.weight(1f).fillMaxWidth()) {
        StoryText(uiState.segments, contentPadding = FinishedStoryPadding)
    }
    WriterLegend(uiState.writers, currentPlayerId = uiState.playerId)
    if (uiState.canPlayAgain) {
        RaisedButton(
            label = stringResource(Res.string.finished_play_again),
            onClick = { onAction(FinishedAction.PlayAgain) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = Res.drawable.ic_replay,
        )
    } else {
        uiState.host?.let { host -> PlayAgainHint(host) }
    }
    FlatButton(stringResource(Res.string.finished_home), onClick = { onAction(FinishedAction.Home) }, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun finishedSummary(
    contributionCount: Int,
    writerCount: Int,
): String =
    stringResource(
        Res.string.finished_summary,
        pluralStringResource(Res.plurals.count_contributions, contributionCount, contributionCount),
        pluralStringResource(Res.plurals.count_writers, writerCount, writerCount),
    )

@Composable
private fun FinishedHeader(summary: String) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StoryCompleteStamp()
        BasicText(
            text = stringResource(Res.string.finished_the_end),
            modifier = Modifier.padding(top = 8.dp).semantics { heading() },
            style = typography.displayMedium.copy(color = colors.onStage, textAlign = TextAlign.Center),
        )
        BasicText(summary, style = typography.bodySmall.copy(color = colors.onStageMuted, textAlign = TextAlign.Center))
    }
}

@Composable
private fun StoryCompleteStamp() {
    val colors = StoryRoundTheme.colors
    BasicText(
        text = stringResource(Res.string.finished_story_complete),
        modifier =
            Modifier
                .tilt(degrees = STAMP_TILT_DEGREES)
                .printedSurface(
                    fill = colors.marigold,
                    ink = colors.ink,
                    shape = StampShape,
                    shadowOffset = 3.dp,
                    shadowColor = colors.ink,
                ).padding(horizontal = 12.dp, vertical = 4.dp),
        style = StoryRoundTheme.typography.eyebrowLarge.copy(color = colors.ink),
    )
}

@Composable
private fun PlayAgainHint(host: PlayerUi) {
    val colors = StoryRoundTheme.colors
    BasicText(
        text = stringResource(Res.string.finished_play_again_hint, host.name),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        style = StoryRoundTheme.typography.bodySmall.copy(color = colors.onStageMuted, textAlign = TextAlign.Center),
    )
}

@PhonePreview
@Composable
private fun FinishedScreenHostPreview() =
    ScreenPreview { FinishedScreen(FinishedPreviewData.finished(PreviewPlayers.alex.id), onAction = {}) }

@PhonePreview
@Composable
private fun FinishedScreenGuestPreview() =
    ScreenPreview { FinishedScreen(FinishedPreviewData.finished(PreviewPlayers.sam.id), onAction = {}) }
