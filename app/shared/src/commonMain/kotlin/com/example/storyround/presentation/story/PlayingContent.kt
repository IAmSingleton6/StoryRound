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
import com.example.storyround.presentation.model.midSentenceNameFor
import com.example.storyround.presentation.model.nameFor
import com.example.storyround.ui.components.PlayerAvatar
import com.example.storyround.ui.components.StageScreen
import com.example.storyround.ui.components.StoryPaper
import com.example.storyround.ui.components.StoryScreenPadding
import com.example.storyround.ui.components.StoryText
import com.example.storyround.ui.components.WaitingPill
import com.example.storyround.ui.components.WritingField
import com.example.storyround.ui.components.printedSurface
import com.example.storyround.ui.components.stagePanel
import com.example.storyround.ui.components.tilt
import com.example.storyround.ui.preview.PhonePreview
import com.example.storyround.ui.preview.PreviewPlayers
import com.example.storyround.ui.preview.ScreenPreview
import com.example.storyround.ui.theme.OutlineWidth
import com.example.storyround.ui.theme.StoryRoundTheme
import com.example.storyround.ui.theme.toColor
import com.example.storyround.ui.theme.toContentColor
import org.jetbrains.compose.resources.stringResource
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.story_continue_placeholder
import storyround.app.shared.generated.resources.story_one_word_placeholder
import storyround.app.shared.generated.resources.story_one_word_tag
import storyround.app.shared.generated.resources.story_picking_word
import storyround.app.shared.generated.resources.story_round_label
import storyround.app.shared.generated.resources.story_seconds_unit
import storyround.app.shared.generated.resources.story_submit
import storyround.app.shared.generated.resources.story_time_left
import storyround.app.shared.generated.resources.story_time_up
import storyround.app.shared.generated.resources.story_turn_label
import storyround.app.shared.generated.resources.story_turn_summary_final
import storyround.app.shared.generated.resources.story_turn_summary_next
import storyround.app.shared.generated.resources.story_turn_tag_player
import storyround.app.shared.generated.resources.story_turn_tag_yours
import storyround.app.shared.generated.resources.story_writing
import storyround.app.shared.generated.resources.writing_field_one_word_only

private const val COLOR_ANIMATION_MILLIS = 300
private const val TOAST_TILT_DEGREES = -2f
private val TurnTagShape = RoundedCornerShape(8.dp)
private val TimeLeftBarShape = RoundedCornerShape(5.dp)
private val TimeLeftBarHeight = 10.dp
private val ToastShape = RoundedCornerShape(14.dp)

private val TimeUpToastTopOffset = 148.dp

@Composable
internal fun PlayingContent(
    uiState: StoryUiState.Playing,
    onAction: (StoryAction) -> Unit,
) = StageScreen(
    contentPadding = StoryScreenPadding,
    isReconnecting = uiState.isReconnecting,
    verticalArrangement = Arrangement.spacedBy(12.dp),
    overlay = {
        uiState.timeUpHandOver?.let { next ->
            TimeUpToast(stringResource(Res.string.story_time_up, next.midSentenceNameFor(uiState.playerId)))
        }
    },
) {
    StoryTopBar(
        label = stringResource(Res.string.story_round_label, uiState.round, uiState.room.maxRounds),
        detail = stringResource(Res.string.story_turn_label, uiState.turn, uiState.totalTurns),
        players = uiState.room.players,
        activePlayerId = uiState.activePlayer.id,
    )
    TurnCard(uiState)
    StoryPaper(Modifier.weight(1f).fillMaxWidth()) {
        StoryText(
            segments = uiState.segments,
            newestTag = uiState.newestAuthor?.nameFor(uiState.playerId)?.uppercase(),
        )
    }
    if (uiState.isYourTurn) {
        WritingField(
            value = uiState.contributionDraft,
            onValueChange = { onAction(StoryAction.ContributionChanged(it)) },
            placeholder =
                stringResource(
                    if (uiState.oneWordPerTurn) Res.string.story_one_word_placeholder else Res.string.story_continue_placeholder,
                ),
            maxLength = uiState.maxDraftLength,
            sendLabel = stringResource(Res.string.story_submit),
            canSend = uiState.canSubmitContribution,
            onSend = { onAction(StoryAction.SubmitContribution) },
            onRandomIdea = if (uiState.oneWordPerTurn) null else ({ onAction(StoryAction.RandomContribution) }),
            error = if (uiState.breaksOneWordRule) stringResource(Res.string.writing_field_one_word_only) else null,
            singleWord = uiState.oneWordPerTurn,
        )
    } else {
        WaitingPill(
            uiState.activePlayer,
            stringResource(
                if (uiState.oneWordPerTurn) Res.string.story_picking_word else Res.string.story_writing,
                uiState.activePlayer.name,
            ),
        )
    }
}

@Composable
private fun TurnCard(uiState: StoryUiState.Playing) {
    val colors = StoryRoundTheme.colors
    val activePlayer = uiState.activePlayer
    val timerColor by animateColorAsState(
        if (uiState.isUrgent) colors.coral else colors.onStage,
        tween(COLOR_ANIMATION_MILLIS),
        label = "timerColor",
    )
    val barColor by animateColorAsState(
        if (uiState.isUrgent) colors.coral else activePlayer.colour.toColor(),
        tween(COLOR_ANIMATION_MILLIS),
        label = "barColor",
    )
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .stagePanel(fill = colors.stageRaised, outline = colors.stageHairline)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            PlayerAvatar(activePlayer, size = 44.dp, borderWidth = OutlineWidth.Regular)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    TurnTag(turnTagLabel(uiState), activePlayer)
                    if (uiState.oneWordPerTurn) OneWordTag()
                }
                BasicText(nextTurnLabel(uiState), style = StoryRoundTheme.typography.bodyXSmall.copy(color = colors.onStageMuted))
            }
            Countdown(uiState.timeLeftText, timerColor)
        }
        TimeLeftBar(uiState.timeLeftFraction, barColor)
    }
}

@Composable
private fun turnTagLabel(uiState: StoryUiState.Playing): String =
    if (uiState.isYourTurn) {
        stringResource(Res.string.story_turn_tag_yours)
    } else {
        stringResource(Res.string.story_turn_tag_player, uiState.activePlayer.name.uppercase())
    }

@Composable
private fun nextTurnLabel(uiState: StoryUiState.Playing): String {
    val next = uiState.nextPlayer ?: return stringResource(Res.string.story_turn_summary_final, uiState.turnSeconds)
    return stringResource(
        Res.string.story_turn_summary_next,
        uiState.turnSeconds,
        next.midSentenceNameFor(uiState.playerId),
        uiState.nextTurnSeconds,
    )
}

@Composable
private fun TurnTag(
    label: String,
    writer: PlayerUi,
) {
    BasicText(
        text = label,
        modifier =
            Modifier
                .semantics { liveRegion = LiveRegionMode.Polite }
                .background(writer.colour.toColor(), TurnTagShape)
                .border(OutlineWidth.Thin, StoryRoundTheme.colors.ink, TurnTagShape)
                .padding(horizontal = 8.dp, vertical = 2.dp),
        style = StoryRoundTheme.typography.titleLarge.copy(color = writer.colour.toContentColor()),
    )
}

@Composable
private fun OneWordTag() {
    val colors = StoryRoundTheme.colors
    BasicText(
        stringResource(Res.string.story_one_word_tag),
        modifier =
            Modifier
                .border(OutlineWidth.Thin, colors.marigoldOutline, TurnTagShape)
                .padding(horizontal = 7.dp, vertical = 3.dp),
        style = StoryRoundTheme.typography.tag.copy(color = colors.marigold),
    )
}

@Composable
private fun Countdown(
    timeLeftText: String,
    color: Color,
) {
    val typography = StoryRoundTheme.typography
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        BasicText(timeLeftText, modifier = Modifier.alignByBaseline(), style = typography.countdown.copy(color = color))
        BasicText(
            stringResource(Res.string.story_seconds_unit),
            modifier = Modifier.alignByBaseline(),
            style = typography.labelSmall.copy(color = color),
        )
    }
}

@Composable
private fun TimeLeftBar(
    fraction: Float,
    color: Color,
) {
    val description = stringResource(Res.string.story_time_left)
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(TimeLeftBarHeight)
                .background(StoryRoundTheme.colors.stageHairline, TimeLeftBarShape)
                .semantics {
                    contentDescription = description
                    progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
                },
    ) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(fraction).background(color, TimeLeftBarShape))
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
                .padding(top = TimeUpToastTopOffset)
                .semantics { liveRegion = LiveRegionMode.Assertive }
                .tilt(degrees = TOAST_TILT_DEGREES)
                .printedSurface(
                    fill = colors.marigold,
                    ink = colors.ink,
                    shape = ToastShape,
                    shadowOffset = 4.dp,
                    shadowColor = colors.ink,
                ).padding(horizontal = 16.dp, vertical = 10.dp),
        style = StoryRoundTheme.typography.labelMedium.copy(color = colors.ink),
        maxLines = 1,
    )
}

@PhonePreview
@Composable
private fun PlayingYourTurnPreview() =
    ScreenPreview { PlayingContent(StoryPreviewData.playing(viewerId = PreviewPlayers.alex.id), onAction = {}) }

@PhonePreview
@Composable
private fun PlayingWaitingPreview() =
    ScreenPreview { PlayingContent(StoryPreviewData.playing(viewerId = PreviewPlayers.sam.id), onAction = {}) }

@PhonePreview
@Composable
private fun PlayingUrgentPreview() =
    ScreenPreview { PlayingContent(StoryPreviewData.playing(viewerId = PreviewPlayers.alex.id, timeLeftMillis = 3_200L), onAction = {}) }

@PhonePreview
@Composable
private fun PlayingTimeUpPreview() =
    ScreenPreview {
        val uiState =
            StoryPreviewData.playing(
                viewerId = PreviewPlayers.priya.id,
                activePlayer = PreviewPlayers.sam,
                timeLeftMillis = 14_000L,
                timeUpHandOver = PreviewPlayers.sam,
            )
        PlayingContent(uiState, onAction = {})
    }

@PhonePreview
@Composable
private fun PlayingReconnectingPreview() =
    ScreenPreview { PlayingContent(StoryPreviewData.playing(viewerId = PreviewPlayers.sam.id, isReconnecting = true), onAction = {}) }
