package com.example.storyround.presentation.story

import androidx.compose.runtime.Composable

@Composable
internal fun StoryScreen(
    uiState: StoryUiState,
    onAction: (StoryAction) -> Unit,
) {
    when (uiState) {
        StoryUiState.Loading -> Unit
        is StoryUiState.OpeningPrompt -> OpeningPromptContent(uiState, onAction)
        is StoryUiState.Playing -> PlayingContent(uiState, onAction)
    }
}
