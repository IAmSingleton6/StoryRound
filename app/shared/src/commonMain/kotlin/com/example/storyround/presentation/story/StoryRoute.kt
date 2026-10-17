package com.example.storyround.presentation.story

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun StoryRoute(viewModel: StoryViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    StoryScreen(uiState = uiState, onAction = viewModel::onAction)
}
