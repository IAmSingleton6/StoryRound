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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.storyround.presentation.model.StoryUi
import com.example.storyround.presentation.model.UiText
import com.example.storyround.presentation.model.asString
import com.example.storyround.ui.components.CircleIconButton
import com.example.storyround.ui.components.ErrorBanner
import com.example.storyround.ui.components.PlayerAvatar
import com.example.storyround.ui.components.RaisedButton
import com.example.storyround.ui.components.RaisedButtonColor
import com.example.storyround.ui.components.RaisedButtonSize
import com.example.storyround.ui.components.StageScreen
import com.example.storyround.ui.components.WriterShareStrip
import com.example.storyround.ui.components.printedSurface
import com.example.storyround.ui.components.tilt
import com.example.storyround.ui.preview.PhonePreview
import com.example.storyround.ui.preview.ScreenPreview
import com.example.storyround.ui.theme.OutlineWidth
import com.example.storyround.ui.theme.StoryRoundTheme
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.back
import storyround.app.shared.generated.resources.count_contributions
import storyround.app.shared.generated.resources.count_players
import storyround.app.shared.generated.resources.error_server_unreachable
import storyround.app.shared.generated.resources.history_back
import storyround.app.shared.generated.resources.history_empty_body
import storyround.app.shared.generated.resources.history_empty_title
import storyround.app.shared.generated.resources.history_story_summary
import storyround.app.shared.generated.resources.history_subtitle
import storyround.app.shared.generated.resources.history_title
import storyround.app.shared.generated.resources.ic_back

private const val HISTORY_CARD_TILT_DEGREES = 0.6f
private const val HISTORY_CARD_AVATAR_LIMIT = 5
private const val EXCERPT_MAX_LINES = 2
private const val BACK_CARD_TILT_DEGREES = 8f
private const val MIDDLE_CARD_TILT_DEGREES = -6f

private val PLACEHOLDER_LINE_WIDTHS = listOf(0.8f, 0.95f, 0.6f)

private val HistoryCardShape = RoundedCornerShape(18.dp)
private val IllustrationCardShape = RoundedCornerShape(14.dp)
private val IllustrationLineShape = RoundedCornerShape(3.dp)
private val IllustrationSize = DpSize(width = 180.dp, height = 130.dp)
private val IllustrationCardSize = DpSize(width = 140.dp, height = 104.dp)
private val PlaceholderLineHeight = 6.dp
private val OverlappingAvatarSize = 24.dp

@Composable
internal fun HistoryScreen(
    uiState: HistoryUiState,
    onAction: (HistoryAction) -> Unit,
) = StageScreen(contentPadding = PaddingValues(top = 8.dp)) {
    HistoryHeader(onBack = { onAction(HistoryAction.Back) })
    when (uiState) {
        HistoryUiState.Loading -> Unit
        is HistoryUiState.Content -> {
            uiState.error?.let { error -> ErrorBanner(error.asString(), Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) }
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
        CircleIconButton(
            icon = Res.drawable.ic_back,
            contentDescription = stringResource(Res.string.back),
            onClick = onBack,
            fill = colors.stageScrim,
            outline = colors.stageOutline,
            tint = colors.onStage,
            iconSize = 20.dp,
        )
        Column {
            BasicText(
                text = stringResource(Res.string.history_title),
                modifier = Modifier.semantics { heading() },
                style = typography.headlineLarge.copy(color = colors.onStage),
            )
            BasicText(stringResource(Res.string.history_subtitle), style = typography.bodyXSmall.copy(color = colors.onStageMuted))
        }
    }
}

@Composable
private fun HistoryList(
    stories: List<StoryUi>,
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
    story: StoryUi,
    tiltDegrees: Float,
    onOpen: () -> Unit,
) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .tilt(degrees = tiltDegrees)
                .printedSurface(
                    fill = colors.paper,
                    ink = colors.ink,
                    shape = HistoryCardShape,
                    shadowOffset = 5.dp,
                    shadowColor = colors.inkShadow,
                ).clip(HistoryCardShape)
                .clickable(role = Role.Button, onClick = onOpen),
    ) {
        WriterShareStrip(story.writers)
        Column(
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BasicText(
                text = story.prompt,
                style = typography.storyExcerpt.copy(color = colors.ink),
                maxLines = EXCERPT_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OverlappingAvatars(story)
                BasicText(storySummary(story), style = typography.labelXSmall.copy(color = colors.onPaperMuted))
            }
        }
    }
}

@Composable
private fun storySummary(story: StoryUi): String {
    val players = story.players.size
    val contributions = story.contributions.size
    return stringResource(
        Res.string.history_story_summary,
        pluralStringResource(Res.plurals.count_players, players, players),
        pluralStringResource(Res.plurals.count_contributions, contributions, contributions),
    )
}

@Composable
private fun OverlappingAvatars(story: StoryUi) {
    val paper = StoryRoundTheme.colors.paper
    Row(Modifier.padding(end = 6.dp), horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
        story.players.take(HISTORY_CARD_AVATAR_LIMIT).forEach { player ->
            PlayerAvatar(player = player, size = OverlappingAvatarSize, borderColor = paper, initialSize = 10.sp)
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
        BasicText(
            stringResource(Res.string.history_empty_title),
            style = typography.headlineMedium.copy(color = colors.onStage, textAlign = TextAlign.Center),
        )
        BasicText(
            stringResource(Res.string.history_empty_body),
            style = typography.bodyMedium.copy(color = colors.onStageMuted, textAlign = TextAlign.Center),
        )
        RaisedButton(
            label = stringResource(Res.string.history_back),
            onClick = onBack,
            modifier = Modifier.padding(top = 10.dp),
            color = RaisedButtonColor.Paper,
            size = RaisedButtonSize.Medium,
            contentPadding = PaddingValues(horizontal = 32.dp),
            textStyle = typography.buttonSmall,
        )
    }
}

@Composable
private fun EmptyHistoryIllustration(modifier: Modifier = Modifier) {
    val colors = StoryRoundTheme.colors
    Box(modifier.size(IllustrationSize)) {
        val card = Modifier.offset(x = 20.dp, y = 10.dp).size(IllustrationCardSize)
        IllustrationCard(colors.stageRaised, card.tilt(degrees = BACK_CARD_TILT_DEGREES))
        IllustrationCard(colors.cue, card.tilt(degrees = MIDDLE_CARD_TILT_DEGREES))
        Column(
            modifier =
                card
                    .printedSurface(
                        fill = colors.paper,
                        ink = colors.ink,
                        shape = IllustrationCardShape,
                        shadowOffset = 5.dp,
                        shadowColor = colors.inkShadow,
                    ).padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PLACEHOLDER_LINE_WIDTHS.forEach { width ->
                Box(Modifier.fillMaxWidth(width).height(PlaceholderLineHeight).background(colors.disabledFill, IllustrationLineShape))
            }
        }
    }
}

@Composable
private fun IllustrationCard(
    color: Color,
    modifier: Modifier,
) {
    Box(modifier.background(color, IllustrationCardShape).border(OutlineWidth.Regular, StoryRoundTheme.colors.ink, IllustrationCardShape))
}

@PhonePreview
@Composable
private fun HistoryScreenPreview() = ScreenPreview { HistoryScreen(HistoryUiState.Content(HistoryPreviewData.stories), onAction = {}) }

@PhonePreview
@Composable
private fun HistoryScreenEmptyPreview() = ScreenPreview { HistoryScreen(HistoryUiState.Content(emptyList()), onAction = {}) }

@PhonePreview
@Composable
private fun HistoryScreenErrorPreview() =
    ScreenPreview {
        val error = UiText.Resource(Res.string.error_server_unreachable)
        HistoryScreen(HistoryUiState.Content(HistoryPreviewData.stories, error = error), onAction = {})
    }
