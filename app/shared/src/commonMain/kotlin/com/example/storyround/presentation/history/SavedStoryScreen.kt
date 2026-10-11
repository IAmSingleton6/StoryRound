package com.example.storyround.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.components.SrIcon
import com.example.storyround.ui.components.SrIcons
import com.example.storyround.ui.components.StageButton
import com.example.storyround.ui.components.StagePillButton
import com.example.storyround.ui.components.StageScaffold
import com.example.storyround.ui.components.StoryPaper
import com.example.storyround.ui.components.StoryText
import com.example.storyround.ui.components.WriterLegend
import com.example.storyround.ui.components.WriterShareStrip
import com.example.storyround.ui.theme.StoryRoundTheme

private val SavedStoryPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 28.dp)

@Composable
internal fun SavedStoryScreen(
    uiState: SavedStoryUiState,
    onBack: () -> Unit,
) = StageScaffold {
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StagePillButton(
                onClick = onBack,
                contentPadding = PaddingValues(horizontal = 13.dp),
                modifier = Modifier.semantics { contentDescription = "Back" },
            ) {
                SrIcon(SrIcons.Back, tint = colors.onStage, modifier = Modifier.size(18.dp))
            }
            BasicText("SAVED STORY", style = typography.eyebrow.copy(color = colors.onStageMuted))
        }
        StoryPaper(Modifier.weight(1f).fillMaxWidth()) {
            Column {
                WriterShareStrip(uiState.writers)
                StoryText(
                    segments = uiState.segments,
                    modifier = Modifier.weight(1f),
                    textStyle = typography.storySaved,
                    contentPadding = SavedStoryPadding,
                )
            }
        }
        WriterLegend(uiState.writers)
        StageButton("Back to history", onClick = onBack, modifier = Modifier.fillMaxWidth())
    }
}
