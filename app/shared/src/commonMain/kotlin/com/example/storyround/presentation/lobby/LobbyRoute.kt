package com.example.storyround.presentation.lobby

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun LobbyRoute(onLeave: () -> Unit) {
    val viewModel = koinViewModel<LobbyViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    LobbyScreen(uiState = uiState) { action ->
        if (action == LobbyAction.Leave) onLeave() else viewModel.onAction(action)
    }
}
