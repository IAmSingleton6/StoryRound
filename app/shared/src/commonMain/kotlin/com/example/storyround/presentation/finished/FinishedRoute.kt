package com.example.storyround.presentation.finished

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun FinishedRoute(
    onHome: () -> Unit,
    viewModel: FinishedViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    FinishedScreen(uiState = uiState) { action ->
        when (action) {
            FinishedAction.Home -> onHome()
            else -> viewModel.onAction(action)
        }
    }
}
