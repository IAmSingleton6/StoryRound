package com.example.storyround.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.components.FlatButton
import com.example.storyround.ui.components.PillButton
import com.example.storyround.ui.components.RoomScreenPadding
import com.example.storyround.ui.components.StageScreen
import com.example.storyround.ui.components.StoryPaper
import com.example.storyround.ui.components.StoryText
import com.example.storyround.ui.components.TintedIcon
import com.example.storyround.ui.components.WriterLegend
import com.example.storyround.ui.components.WriterShareStrip
import com.example.storyround.ui.preview.PhonePreview
import com.example.storyround.ui.preview.ScreenPreview
import com.example.storyround.ui.theme.StoryRoundTheme
import org.jetbrains.compose.resources.stringResource
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.back
import storyround.app.shared.generated.resources.ic_back
import storyround.app.shared.generated.resources.saved_story_back_to_history
import storyround.app.shared.generated.resources.saved_story_title

private val SavedStoryPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 28.dp)

@Composable
internal fun SavedStoryScreen(
    uiState: SavedStoryUiState,
    onBack: () -> Unit,
) = StageScreen(contentPadding = RoomScreenPadding, verticalArrangement = Arrangement.spacedBy(14.dp)) {
    SavedStoryTopBar(onBack)
    StoryPaper(Modifier.weight(1f).fillMaxWidth()) {
        Column {
            WriterShareStrip(uiState.writers)
            StoryText(
                segments = uiState.segments,
                modifier = Modifier.weight(1f),
                contentPadding = SavedStoryPadding,
            )
        }
    }
    WriterLegend(uiState.writers)
    FlatButton(stringResource(Res.string.saved_story_back_to_history), onClick = onBack, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun SavedStoryTopBar(onBack: () -> Unit) {
    val colors = StoryRoundTheme.colors
    val backDescription = stringResource(Res.string.back)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PillButton(
            onClick = onBack,
            contentPadding = PaddingValues(horizontal = 13.dp),
            modifier = Modifier.semantics { contentDescription = backDescription },
        ) {
            TintedIcon(Res.drawable.ic_back, tint = colors.onStage, modifier = Modifier.size(18.dp))
        }
        BasicText(
            stringResource(Res.string.saved_story_title),
            style = StoryRoundTheme.typography.eyebrowMedium.copy(color = colors.onStageMuted),
        )
    }
}

@PhonePreview
@Composable
private fun SavedStoryScreenPreview() = ScreenPreview { SavedStoryScreen(HistoryPreviewData.savedStory(), onBack = {}) }
