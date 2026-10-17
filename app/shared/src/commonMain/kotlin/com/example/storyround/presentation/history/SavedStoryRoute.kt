package com.example.storyround.presentation.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SavedStoryRoute(
    onBack: () -> Unit,
    viewModel: SavedStoryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    SavedStoryScreen(uiState = uiState, onBack = onBack)
}
