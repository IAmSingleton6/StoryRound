package com.example.storyround.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.storyround.presentation.model.HistoryStoryUi
import com.example.storyround.ui.components.ErrorBanner
import com.example.storyround.ui.components.PlayerAvatar
import com.example.storyround.ui.components.SrButton
import com.example.storyround.ui.components.SrButtonSize
import com.example.storyround.ui.components.SrButtonVariant
import com.example.storyround.ui.components.SrIcon
import com.example.storyround.ui.components.SrIcons
import com.example.storyround.ui.components.StageScaffold
import com.example.storyround.ui.components.WriterShareStrip
import com.example.storyround.ui.components.hardShadow
import com.example.storyround.ui.components.tilt
import com.example.storyround.ui.theme.StoryRoundTheme

private const val HISTORY_CARD_TILT_DEGREES = 0.6f
private const val HISTORY_CARD_AVATAR_LIMIT = 5
private const val EXCERPT_MAX_LINES = 2
private val HistoryCardShape = RoundedCornerShape(18.dp)
private val IllustrationCardShape = RoundedCornerShape(14.dp)
private val IllustrationLineShape = RoundedCornerShape(3.dp)

@Composable
internal fun HistoryScreen(
    uiState: HistoryUiState,
    onAction: (HistoryAction) -> Unit,
) = StageScaffold {
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(top = 8.dp)) {
        HistoryHeader(onBack = { onAction(HistoryAction.Back) })
        when (uiState) {
            HistoryUiState.Loading -> Unit
            is HistoryUiState.Error -> ErrorBanner(uiState.message, Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
            is HistoryUiState.Content ->
                if (uiState.stories.isEmpty()) {
                    EmptyHistory(onBack = { onAction(HistoryAction.Back) })
                } else {
                    HistoryList(uiState.stories, onOpen = { onAction(HistoryAction.OpenStory(it)) })
                }
        }
    }
}

@Composable
private fun HistoryHeader(onBack: () -> Unit) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(44.dp)
                    .background(colors.stageScrim, CircleShape)
                    .border(2.dp, colors.stageOutline, CircleShape)
                    .clickable(role = Role.Button, onClick = onBack)
                    .semantics { contentDescription = "Back" },
            contentAlignment = Alignment.Center,
        ) {
            SrIcon(SrIcons.Back, tint = colors.onStage, modifier = Modifier.size(20.dp))
        }
        Column {
            BasicText(
                text = "Story history",
                modifier = Modifier.semantics { heading() },
                style = typography.screenTitle.copy(color = colors.onStage),
            )
            BasicText("Finished stories saved on this device", style = typography.meta.copy(color = colors.onStageMuted))
        }
    }
}

@Composable
private fun HistoryList(
    stories: List<HistoryStoryUi>,
    onOpen: (String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        itemsIndexed(stories, key = { _, story -> story.id }) { index, story ->
            val tilt = if (index % 2 == 0) -HISTORY_CARD_TILT_DEGREES else HISTORY_CARD_TILT_DEGREES
            HistoryCard(story, tilt, onOpen = { onOpen(story.id) })
        }
    }
}

@Composable
private fun HistoryCard(
    story: HistoryStoryUi,
    tiltDegrees: Float,
    onOpen: () -> Unit,
) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .tilt(degrees = tiltDegrees, dx = 0.dp, dy = 0.dp)
                .hardShadow(offsetY = 5.dp, color = colors.inkShadow, shape = HistoryCardShape)
                .background(colors.paper, HistoryCardShape)
                .border(2.5.dp, colors.ink, HistoryCardShape)
                .clip(HistoryCardShape)
                .clickable(role = Role.Button, onClick = onOpen),
    ) {
        WriterShareStrip(story.writers)
        Column(
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BasicText(
                text = story.opening,
                style = typography.storyExcerpt.copy(color = colors.ink),
                maxLines = EXCERPT_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OverlappingAvatars(story)
                BasicText(
                    "${story.players.size} players · ${story.contributionCount} contributions",
                    style = typography.metaStrong.copy(color = colors.onPaperMuted),
                )
            }
        }
    }
}

@Composable
private fun OverlappingAvatars(story: HistoryStoryUi) {
    val paper = StoryRoundTheme.colors.paper
    Row(Modifier.padding(end = 6.dp), horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
        story.players.take(HISTORY_CARD_AVATAR_LIMIT).forEach { player ->
            PlayerAvatar(player = player, size = 24.dp, borderColor = paper, initialSize = 10.sp)
        }
    }
}

@Composable
private fun ColumnScope.EmptyHistory(onBack: () -> Unit) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Column(
        modifier = Modifier.weight(1f).fillMaxWidth().padding(start = 40.dp, end = 40.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmptyHistoryIllustration(Modifier.padding(bottom = 18.dp))
        BasicText("No completed stories yet.", style = typography.emptyTitle.copy(color = colors.onStage, textAlign = TextAlign.Center))
        BasicText(
            "Finish a game and the story is saved here, on this device.",
            style = typography.body.copy(color = colors.onStageMuted, textAlign = TextAlign.Center),
        )
        SrButton(
            label = "BACK",
            onClick = onBack,
            modifier = Modifier.padding(top = 10.dp),
            variant = SrButtonVariant.Paper,
            size = SrButtonSize.Medium,
            contentPadding = PaddingValues(horizontal = 32.dp),
            textStyle = typography.sendButton,
        )
    }
}

@Composable
private fun EmptyHistoryIllustration(modifier: Modifier = Modifier) {
    val colors = StoryRoundTheme.colors
    Box(modifier.size(width = 180.dp, height = 130.dp)) {
        val card = Modifier.offset(x = 20.dp, y = 10.dp).size(width = 140.dp, height = 104.dp)
        IllustrationCard(colors.stageRaised, card.tilt(degrees = 8f, dx = 0.dp, dy = 0.dp))
        IllustrationCard(colors.cue, card.tilt(degrees = -6f, dx = 0.dp, dy = 0.dp))
        Column(
            modifier =
                card
                    .hardShadow(offsetY = 5.dp, color = colors.inkShadow, shape = IllustrationCardShape)
                    .background(colors.paper, IllustrationCardShape)
                    .border(2.5.dp, colors.ink, IllustrationCardShape)
                    .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            listOf(0.8f, 0.95f, 0.6f).forEach { width ->
                Box(Modifier.fillMaxWidth(width).height(6.dp).background(colors.disabledFill, IllustrationLineShape))
            }
        }
    }
}

@Composable
private fun IllustrationCard(
    color: Color,
    modifier: Modifier,
) {
    Box(modifier.background(color, IllustrationCardShape).border(2.5.dp, StoryRoundTheme.colors.ink, IllustrationCardShape))
}
