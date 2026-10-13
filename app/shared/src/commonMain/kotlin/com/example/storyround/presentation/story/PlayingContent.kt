package com.example.storyround.presentation.story

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.protocol.MAX_STORY_TEXT_LENGTH
import com.example.storyround.ui.components.PlayerAvatar
import com.example.storyround.ui.components.ReconnectingBanner
import com.example.storyround.ui.components.StageBackground
import com.example.storyround.ui.components.StoryPaper
import com.example.storyround.ui.components.StoryText
import com.example.storyround.ui.components.WaitingPill
import com.example.storyround.ui.components.WritingField
import com.example.storyround.ui.components.hardShadow
import com.example.storyround.ui.components.tilt
import com.example.storyround.ui.theme.StoryRoundTheme
import com.example.storyround.ui.theme.toColor
import com.example.storyround.ui.theme.toContentColor

private const val COLOUR_ANIMATION_MILLIS = 300
private val CardShape = RoundedCornerShape(20.dp)
private val TurnTagShape = RoundedCornerShape(8.dp)
private val BarShape = RoundedCornerShape(5.dp)
private val ToastShape = RoundedCornerShape(14.dp)

@Composable
internal fun PlayingContent(
    uiState: StoryUiState.Playing,
    onAction: (StoryAction) -> Unit,
) = StageBackground {
    val activePlayer = uiState.activePlayer ?: return@StageBackground
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StoryTopBar(
            label = "ROUND ${uiState.round} OF ${uiState.room.maxRounds}",
            detail = "Turn ${uiState.turn} of ${uiState.totalTurns}",
            players = uiState.room.players,
            activePlayerId = activePlayer.id,
        )
        TurnCard(uiState, activePlayer)
        StoryPaper(Modifier.weight(1f).fillMaxWidth()) {
            StoryText(
                segments = uiState.segments,
                newestTag = uiState.newestTag,
            )
        }
        if (uiState.isYourTurn) {
            WritingField(
                value = uiState.contributionDraft,
                onValueChange = { onAction(StoryAction.ContributionChanged(it)) },
                placeholder = "Continue the story…",
                maxLength = MAX_STORY_TEXT_LENGTH,
                isNearLimit = uiState.isDraftNearLimit,
                sendLabel = "SUBMIT",
                canSend = uiState.canSubmitContribution,
                onSend = { onAction(StoryAction.SubmitContribution) },
                onRandomIdea = { onAction(StoryAction.RandomContribution) },
            )
        } else {
            WaitingPill(activePlayer, "${activePlayer.name} is writing…")
        }
    }
    uiState.timeUpHandOver?.let { next -> TimeUpToast("Time’s up! Over to ${uiState.displayName(next, lowercaseYou = true)}.") }
    if (uiState.isReconnecting) {
        ReconnectingBanner(
            Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun TurnCard(
    uiState: StoryUiState.Playing,
    activePlayer: PlayerUi,
) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    val playerColour = activePlayer.colour.toColor()
    val timerColour by animateColorAsState(
        if (uiState.isUrgent) colors.coral else colors.onStage,
        tween(COLOUR_ANIMATION_MILLIS),
        label = "timerColour",
    )
    val barColour by animateColorAsState(
        if (uiState.isUrgent) colors.coral else playerColour,
        tween(COLOUR_ANIMATION_MILLIS),
        label = "barColour",
    )
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colors.stageRaised, CardShape)
                .border(2.dp, colors.stageHairline, CardShape)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            PlayerAvatar(activePlayer, size = 44.dp, borderWidth = 2.5.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BasicText(
                    text = if (uiState.isYourTurn) "YOUR TURN" else "${activePlayer.name.uppercase()}’S TURN",
                    modifier =
                        Modifier
                            .semantics { liveRegion = LiveRegionMode.Polite }
                            .background(playerColour, TurnTagShape)
                            .border(2.dp, colors.ink, TurnTagShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    style = typography.turnTag.copy(color = activePlayer.colour.toContentColor()),
                )
                BasicText(uiState.nextTurnLabel, style = typography.meta.copy(color = colors.onStageMuted))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                BasicText(
                    uiState.timeLeftText,
                    modifier = Modifier.alignByBaseline(),
                    style = typography.countdown.copy(color = timerColour),
                )
                BasicText("s", modifier = Modifier.alignByBaseline(), style = typography.callout.copy(color = timerColour))
            }
        }
        TimeLeftBar(uiState.timeLeftFraction, barColour)
    }
}

@Composable
private fun TimeLeftBar(
    fraction: Float,
    colour: Color,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(StoryRoundTheme.colors.stageHairline, BarShape)
                .semantics {
                    contentDescription = "Time left"
                    progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
                },
    ) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(fraction).background(colour, BarShape))
    }
}

@Composable
private fun BoxScope.TimeUpToast(message: String) {
    val colors = StoryRoundTheme.colors
    BasicText(
        text = message,
        modifier =
            Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(top = 148.dp)
                .semantics { liveRegion = LiveRegionMode.Assertive }
                .tilt(degrees = -2f, dx = 0.dp, dy = 0.dp)
                .hardShadow(offsetY = 4.dp, color = colors.ink, shape = ToastShape)
                .background(colors.marigold, ToastShape)
                .border(2.5.dp, colors.ink, ToastShape)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        style = StoryRoundTheme.typography.toast.copy(color = colors.ink),
        maxLines = 1,
    )
}
