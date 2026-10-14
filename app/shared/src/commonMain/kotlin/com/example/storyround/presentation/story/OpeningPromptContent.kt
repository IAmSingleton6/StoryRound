package com.example.storyround.presentation.story

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
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
import com.example.storyround.ui.components.ReconnectingBanner
import com.example.storyround.ui.components.StageBackground
import com.example.storyround.ui.components.StoryPaper
import com.example.storyround.ui.components.StoryRoundIcons
import com.example.storyround.ui.components.TintedIcon
import com.example.storyround.ui.components.WaitingPill
import com.example.storyround.ui.components.WritingField
import com.example.storyround.ui.components.dashedBorder
import com.example.storyround.ui.theme.StoryRoundTheme

private val CardShape = RoundedCornerShape(20.dp)
private val NoTimerShape = RoundedCornerShape(8.dp)

@Composable
internal fun OpeningPromptContent(
    uiState: StoryUiState.OpeningPrompt,
    onAction: (StoryAction) -> Unit,
) = StageBackground {
    val openerName = uiState.openingPlayer?.name.orEmpty()
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StoryTopBar(
            label = "OPENING",
            detail = "No timer yet",
            players = uiState.room.players,
            activePlayerId = uiState.openingPlayer?.id,
        )
        OpeningPromptCard(
            subtitle =
                when {
                    uiState.isOpeningWriter && uiState.oneWordPerTurn -> "Write the first word. Take your time."
                    uiState.isOpeningWriter -> "Write the opening line or premise. Take your time."
                    uiState.oneWordPerTurn -> "$openerName is choosing the first word…"
                    else -> "$openerName is starting the story…"
                },
        )
        StoryPaper(Modifier.weight(1f).fillMaxWidth()) { EmptyStory(uiState.oneWordPerTurn) }
        if (uiState.isOpeningWriter) {
            WritingField(
                value = uiState.openingPromptDraft,
                onValueChange = { onAction(StoryAction.OpeningPromptChanged(it)) },
                placeholder = if (uiState.oneWordPerTurn) "Start with one word…" else "Start the story…",
                maxLength = uiState.maxDraftLength,
                isNearLimit = uiState.isDraftNearLimit,
                sendLabel = "START STORY",
                canSend = uiState.canSubmitOpeningPrompt,
                onSend = { onAction(StoryAction.SubmitOpeningPrompt) },
                onRandomIdea = if (uiState.oneWordPerTurn) null else ({ onAction(StoryAction.RandomOpeningPrompt) }),
                error = uiState.draftError,
                singleWord = uiState.oneWordPerTurn,
            )
        } else {
            uiState.openingPlayer?.let { opener -> WaitingPill(opener, "Waiting for $openerName…") }
        }
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
private fun OpeningPromptCard(subtitle: String) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colors.stageRaised, CardShape)
                .border(2.dp, colors.stageHairline, CardShape)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(44.dp)
                    .background(colors.marigold, CircleShape)
                    .border(2.5.dp, colors.ink, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            TintedIcon(StoryRoundIcons.Feather, tint = colors.ink, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BasicText(
                "START THE STORY",
                modifier = Modifier.semantics { heading() },
                style = typography.heading.copy(color = colors.onStage),
            )
            BasicText(subtitle, style = typography.meta.copy(color = colors.onStageMuted))
        }
        BasicText(
            "NO TIMER",
            modifier =
                Modifier
                    .border(2.dp, colors.marigoldOutline, NoTimerShape)
                    .padding(horizontal = 7.dp, vertical = 3.dp),
            style = typography.tag.copy(color = colors.marigold),
        )
    }
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
            modifier = Modifier.size(64.dp).dashedBorder(2.5.dp, colors.onPaperMuted, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            TintedIcon(StoryRoundIcons.Feather, tint = colors.onPaperMuted, modifier = Modifier.size(28.dp))
        }
        BasicText("Start the story…", style = typography.storyPlaceholder.copy(color = colors.onPaperMuted))
        BasicText(
            text =
                if (oneWordPerTurn) {
                    "The first word becomes the first coloured piece of the story."
                } else {
                    "The opening line becomes the first coloured piece of the story."
                },
            modifier = Modifier.widthIn(max = 250.dp),
            style = typography.caption.copy(color = colors.onPaperMuted, textAlign = TextAlign.Center),
        )
    }
}
