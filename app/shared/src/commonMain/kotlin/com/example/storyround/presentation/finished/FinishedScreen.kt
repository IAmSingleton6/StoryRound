package com.example.storyround.presentation.finished

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.storyround.presentation.model.countOf
import com.example.storyround.ui.components.FlatButton
import com.example.storyround.ui.components.RaisedButton
import com.example.storyround.ui.components.ReconnectingBanner
import com.example.storyround.ui.components.StageBackground
import com.example.storyround.ui.components.StoryPaper
import com.example.storyround.ui.components.StoryRoundIcons
import com.example.storyround.ui.components.StoryText
import com.example.storyround.ui.components.WriterLegend
import com.example.storyround.ui.components.hardShadow
import com.example.storyround.ui.components.tilt
import com.example.storyround.ui.theme.StoryRoundTheme

private val StampShape = RoundedCornerShape(10.dp)
private val FinishedStoryPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 24.dp)

@Composable
internal fun FinishedScreen(
    uiState: FinishedUiState,
    onAction: (FinishedAction) -> Unit,
) = StageBackground {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        FinishedHeader(uiState)
        StoryPaper(Modifier.weight(1f).fillMaxWidth()) {
            StoryText(uiState.segments, textStyle = typography.storyFinished, contentPadding = FinishedStoryPadding)
        }
        WriterLegend(uiState.writers, currentPlayerId = uiState.playerId)
        if (uiState.canPlayAgain) {
            RaisedButton(
                label = "PLAY AGAIN",
                onClick = { onAction(FinishedAction.PlayAgain) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = StoryRoundIcons.Replay,
            )
        } else {
            uiState.host?.let { host ->
                BasicText(
                    text = "${host.name} can start another round from here.",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    style = typography.caption.copy(color = colors.onStageMuted, textAlign = TextAlign.Center),
                )
            }
        }
        FlatButton("Home", onClick = { onAction(FinishedAction.Home) }, modifier = Modifier.fillMaxWidth())
    }
    if (uiState.isReconnecting) {
        ReconnectingBanner(
            Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun FinishedHeader(uiState: FinishedUiState) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicText(
            text = "STORY COMPLETE",
            modifier =
                Modifier
                    .tilt(degrees = -3f, dx = 0.dp, dy = 0.dp)
                    .hardShadow(offsetY = 3.dp, color = colors.ink, shape = StampShape)
                    .background(colors.marigold, StampShape)
                    .border(2.5.dp, colors.ink, StampShape)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            style = typography.stamp.copy(color = colors.ink),
        )
        BasicText(
            text = "The End.",
            modifier = Modifier.padding(top = 8.dp).semantics { heading() },
            style = typography.headline.copy(color = colors.onStage, textAlign = TextAlign.Center),
        )
        BasicText(
            text = "${countOf(
                uiState.contributionCount,
                "contribution",
            )} · ${countOf(uiState.writers.size, "writer")} · saved to your history",
            style = typography.caption.copy(color = colors.onStageMuted, textAlign = TextAlign.Center),
        )
    }
}
