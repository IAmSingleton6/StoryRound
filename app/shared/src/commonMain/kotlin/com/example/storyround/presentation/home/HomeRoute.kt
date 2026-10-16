package com.example.storyround.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HomeRoute(
    onOpenHistory: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreen(uiState = uiState) { action ->
        if (action == HomeAction.OpenHistory) onOpenHistory() else viewModel.onAction(action)
    }
}
