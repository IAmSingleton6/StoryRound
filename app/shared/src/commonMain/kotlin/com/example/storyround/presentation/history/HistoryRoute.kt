package com.example.storyround.presentation.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HistoryRoute(onBack: () -> Unit) {
    val viewModel = koinViewModel<HistoryViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    HistoryScreen(uiState = uiState) { action ->
        if (action == HistoryAction.Back) onBack() else viewModel.onAction(action)
    }
}
