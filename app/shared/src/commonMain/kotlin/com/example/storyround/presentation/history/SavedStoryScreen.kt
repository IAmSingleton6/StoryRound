package com.example.storyround.presentation.history

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.storyround.ui.components.Action
import com.example.storyround.ui.components.Page
import com.example.storyround.ui.components.Story
import com.example.storyround.ui.components.StoryPlayers

@Composable
internal fun SavedStoryScreen(
    uiState: SavedStoryUiState,
    onBack: () -> Unit,
) = Page {
    val story = uiState.story ?: return@Page
    Text("SAVED STORY", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
    Story(story.prompt, story.promptAuthor, story.contributions, Modifier.weight(1f).fillMaxWidth())
    StoryPlayers(story.players)
    Action("BACK TO HISTORY", onClick = onBack, filled = false)
}
