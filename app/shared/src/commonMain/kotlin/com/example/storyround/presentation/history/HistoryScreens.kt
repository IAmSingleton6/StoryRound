package com.example.storyround.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.storyround.protocol.StorySummary
import com.example.storyround.ui.components.Action
import com.example.storyround.ui.components.Page

@Composable
internal fun HistoryScreen(
    uiState: HistoryUiState,
    onAction: (HistoryAction) -> Unit,
) = Page {
    Text("STORY HISTORY", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
    when (uiState) {
        HistoryUiState.Loading -> Text("Loading saved stories...")
        is HistoryUiState.Error -> Text(uiState.message, color = MaterialTheme.colorScheme.error)
        is HistoryUiState.Content -> {
            if (uiState.stories.isEmpty()) {
                Text("No completed stories yet.")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(uiState.stories, key = StorySummary::id) { story ->
                        Card(
                            onClick = { onAction(HistoryAction.OpenStory(story.id)) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(story.prompt, fontWeight = FontWeight.Bold, maxLines = 2)
                                Text(
                                    "${story.playerCount} players · ${story.contributionCount} contributions",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    Action("BACK", onClick = { onAction(HistoryAction.Back) }, filled = false)
}
