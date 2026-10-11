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
import androidx.compose.foundation.layout.height
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
import com.example.storyround.presentation.model.MAX_CONTRIBUTION_LENGTH
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.ui.components.PlayerStrip
import com.example.storyround.ui.components.ReconnectingBanner
import com.example.storyround.ui.components.SrIcon
import com.example.storyround.ui.components.SrIcons
import com.example.storyround.ui.components.StageScaffold
import com.example.storyround.ui.components.StoryComposer
import com.example.storyround.ui.components.StoryPaper
import com.example.storyround.ui.components.TurnWaitingPill
import com.example.storyround.ui.components.dashedBorder
import com.example.storyround.ui.theme.StoryRoundTheme

private val CardShape = RoundedCornerShape(20.dp)
private val NoTimerShape = RoundedCornerShape(8.dp)

@Composable
internal fun OpeningPromptContent(
    uiState: StoryUiState.OpeningPrompt,
    onAction: (StoryAction) -> Unit,
) = StageScaffold {
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
                if (uiState.isOpeningWriter) {
                    "Write the opening line or premise. Take your time."
                } else {
                    "$openerName is starting the story…"
                },
        )
        StoryPaper(Modifier.weight(1f).fillMaxWidth()) { EmptyStory() }
        if (uiState.isOpeningWriter) {
            StoryComposer(
                value = uiState.openingPromptDraft,
                onValueChange = { onAction(StoryAction.OpeningPromptChanged(it)) },
                placeholder = "Start the story…",
                maxLength = MAX_CONTRIBUTION_LENGTH,
                isNearLimit = uiState.isDraftNearLimit,
                sendLabel = "START STORY",
                canSend = uiState.canSubmitOpeningPrompt,
                onSend = { onAction(StoryAction.SubmitOpeningPrompt) },
                onRandomIdea = { onAction(StoryAction.RandomOpeningPrompt) },
            )
        } else {
            uiState.openingPlayer?.let { opener -> TurnWaitingPill(opener, "Waiting for $openerName…") }
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
internal fun StoryTopBar(
    label: String,
    detail: String,
    players: List<PlayerUi>,
    activePlayerId: String?,
) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth().height(40.dp).padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            BasicText(label, style = typography.eyebrowSmall.copy(color = colors.onStageMuted))
            BasicText(detail, style = typography.pill.copy(color = colors.onStage))
        }
        PlayerStrip(players, activePlayerId)
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
            SrIcon(SrIcons.Feather, tint = colors.ink, modifier = Modifier.size(22.dp))
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
private fun EmptyStory() {
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
            SrIcon(SrIcons.Feather, tint = colors.onPaperMuted, modifier = Modifier.size(28.dp))
        }
        BasicText("Start the story…", style = typography.storyPlaceholder.copy(color = colors.onPaperMuted))
        BasicText(
            text = "The opening line becomes the first coloured piece of the story.",
            modifier = Modifier.widthIn(max = 250.dp),
            style = typography.caption.copy(color = colors.onPaperMuted, textAlign = TextAlign.Center),
        )
    }
}
