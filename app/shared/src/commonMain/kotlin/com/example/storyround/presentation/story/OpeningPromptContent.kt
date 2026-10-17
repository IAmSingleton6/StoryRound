package com.example.storyround.presentation.story

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.components.StageScreen
import com.example.storyround.ui.components.StoryPaper
import com.example.storyround.ui.components.StoryScreenPadding
import com.example.storyround.ui.components.TintedIcon
import com.example.storyround.ui.components.WaitingPill
import com.example.storyround.ui.components.WritingField
import com.example.storyround.ui.components.dashedBorder
import com.example.storyround.ui.components.stagePanel
import com.example.storyround.ui.preview.PhonePreview
import com.example.storyround.ui.preview.ScreenPreview
import com.example.storyround.ui.theme.OutlineWidth
import com.example.storyround.ui.theme.StoryRoundTheme
import org.jetbrains.compose.resources.stringResource
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.ic_feather
import storyround.app.shared.generated.resources.story_no_timer
import storyround.app.shared.generated.resources.story_no_timer_yet
import storyround.app.shared.generated.resources.story_opening
import storyround.app.shared.generated.resources.story_opening_hint
import storyround.app.shared.generated.resources.story_opening_one_word_hint
import storyround.app.shared.generated.resources.story_opening_one_word_subtitle_waiting
import storyround.app.shared.generated.resources.story_opening_one_word_subtitle_writer
import storyround.app.shared.generated.resources.story_opening_subtitle_waiting
import storyround.app.shared.generated.resources.story_opening_subtitle_writer
import storyround.app.shared.generated.resources.story_opening_waiting
import storyround.app.shared.generated.resources.story_opening_writer_unknown
import storyround.app.shared.generated.resources.story_start_one_word_placeholder
import storyround.app.shared.generated.resources.story_start_placeholder
import storyround.app.shared.generated.resources.story_start_story
import storyround.app.shared.generated.resources.story_start_the_story
import storyround.app.shared.generated.resources.writing_field_one_word_only

private val NoTimerShape = RoundedCornerShape(8.dp)
private val QuillBadgeSize = 44.dp
private val EmptyStoryBadgeSize = 64.dp
private val EmptyStoryHintMaxWidth = 250.dp

@Composable
internal fun OpeningPromptContent(
    uiState: StoryUiState.OpeningPrompt,
    onAction: (StoryAction) -> Unit,
) = StageScreen(
    contentPadding = StoryScreenPadding,
    isReconnecting = uiState.isReconnecting,
    verticalArrangement = Arrangement.spacedBy(12.dp),
) {
    StoryTopBar(
        label = stringResource(Res.string.story_opening),
        detail = stringResource(Res.string.story_no_timer_yet),
        players = uiState.room.players,
        activePlayerId = uiState.openingPlayer?.id,
    )
    OpeningPromptCard(subtitle = openingSubtitle(uiState))
    StoryPaper(Modifier.weight(1f).fillMaxWidth()) { EmptyStory(uiState.oneWordPerTurn) }
    if (uiState.isOpeningWriter) {
        WritingField(
            value = uiState.openingPromptDraft,
            onValueChange = { onAction(StoryAction.OpeningPromptChanged(it)) },
            placeholder =
                stringResource(
                    if (uiState.oneWordPerTurn) Res.string.story_start_one_word_placeholder else Res.string.story_start_placeholder,
                ),
            maxLength = uiState.maxDraftLength,
            sendLabel = stringResource(Res.string.story_start_story),
            canSend = uiState.canSubmitOpeningPrompt,
            onSend = { onAction(StoryAction.SubmitOpeningPrompt) },
            onRandomIdea = if (uiState.oneWordPerTurn) null else ({ onAction(StoryAction.RandomOpeningPrompt) }),
            error = if (uiState.breaksOneWordRule) stringResource(Res.string.writing_field_one_word_only) else null,
            singleWord = uiState.oneWordPerTurn,
        )
    } else {
        uiState.openingPlayer?.let { opener -> WaitingPill(opener, stringResource(Res.string.story_opening_waiting, opener.name)) }
    }
}

@Composable
private fun openingSubtitle(uiState: StoryUiState.OpeningPrompt): String {
    if (uiState.isOpeningWriter) {
        return stringResource(
            if (uiState.oneWordPerTurn) Res.string.story_opening_one_word_subtitle_writer else Res.string.story_opening_subtitle_writer,
        )
    }
    val openerName = uiState.openingPlayer?.name ?: stringResource(Res.string.story_opening_writer_unknown)
    return stringResource(
        if (uiState.oneWordPerTurn) Res.string.story_opening_one_word_subtitle_waiting else Res.string.story_opening_subtitle_waiting,
        openerName,
    )
}

@Composable
private fun OpeningPromptCard(subtitle: String) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .stagePanel(fill = colors.stageRaised, outline = colors.stageHairline)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QuillBadge()
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BasicText(
                stringResource(Res.string.story_start_the_story),
                modifier = Modifier.semantics { heading() },
                style = typography.titleLarge.copy(color = colors.onStage),
            )
            BasicText(subtitle, style = typography.bodyXSmall.copy(color = colors.onStageMuted))
        }
        NoTimerTag()
    }
}

@Composable
private fun QuillBadge() {
    val colors = StoryRoundTheme.colors
    Box(
        modifier =
            Modifier
                .size(QuillBadgeSize)
                .background(colors.marigold, CircleShape)
                .border(OutlineWidth.Regular, colors.ink, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        TintedIcon(Res.drawable.ic_feather, tint = colors.ink, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun NoTimerTag() {
    val colors = StoryRoundTheme.colors
    BasicText(
        stringResource(Res.string.story_no_timer),
        modifier =
            Modifier
                .border(OutlineWidth.Thin, colors.marigoldOutline, NoTimerShape)
                .padding(horizontal = 7.dp, vertical = 3.dp),
        style = StoryRoundTheme.typography.tag.copy(color = colors.marigold),
    )
}

@Composable
private fun EmptyStory(oneWordPerTurn: Boolean) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(EmptyStoryBadgeSize).dashedBorder(OutlineWidth.Regular, colors.onPaperMuted, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            TintedIcon(Res.drawable.ic_feather, tint = colors.onPaperMuted, modifier = Modifier.size(28.dp))
        }
        BasicText(stringResource(Res.string.story_start_placeholder), style = typography.storyPlaceholder.copy(color = colors.onPaperMuted))
        BasicText(
            text = stringResource(if (oneWordPerTurn) Res.string.story_opening_one_word_hint else Res.string.story_opening_hint),
            modifier = Modifier.widthIn(max = EmptyStoryHintMaxWidth),
            style = typography.bodySmall.copy(color = colors.onPaperMuted, textAlign = TextAlign.Center),
        )
    }
}

@PhonePreview
@Composable
private fun OpeningPromptWriterPreview() =
    ScreenPreview { OpeningPromptContent(StoryPreviewData.openingPrompt(isOpeningWriter = true), onAction = {}) }

@PhonePreview
@Composable
private fun OpeningPromptWaitingPreview() =
    ScreenPreview { OpeningPromptContent(StoryPreviewData.openingPrompt(isOpeningWriter = false), onAction = {}) }
