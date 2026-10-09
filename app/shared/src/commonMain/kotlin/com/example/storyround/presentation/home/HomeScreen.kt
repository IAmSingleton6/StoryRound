package com.example.storyround.presentation.home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.components.Action
import com.example.storyround.ui.components.Page
import com.example.storyround.ui.theme.StoryRoundInk

@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit,
) = Page {
    Spacer(Modifier.height(48.dp))
    Text(
        "STORY RELAY",
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Black,
        color = StoryRoundInk,
    )
    Text(
        "Write a story. One sentence at a time. Less time every turn.",
        style = MaterialTheme.typography.titleMedium,
    )
    OutlinedTextField(
        value = uiState.playerName,
        onValueChange = { onAction(HomeAction.PlayerNameChanged(it)) },
        label = { Text("Your name") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    if (uiState.isJoining) {
        OutlinedTextField(
            value = uiState.roomCode,
            onValueChange = { onAction(HomeAction.RoomCodeChanged(it)) },
            label = { Text("Room code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
    uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    Spacer(Modifier.weight(1f))
    if (uiState.isJoining) {
        Action(
            label = "JOIN GAME",
            onClick = { onAction(HomeAction.JoinGame) },
            enabled = uiState.playerName.isNotBlank() && uiState.roomCode.isNotBlank() && !uiState.isConnecting,
        )
        Action("CANCEL", onClick = { onAction(HomeAction.CancelJoin) }, filled = false)
    } else {
        Action(
            label = "CREATE GAME",
            onClick = { onAction(HomeAction.CreateGame) },
            enabled = uiState.playerName.isNotBlank() && !uiState.isConnecting,
        )
        Action(
            label = "JOIN GAME",
            onClick = { onAction(HomeAction.ShowJoinGame) },
            enabled = uiState.playerName.isNotBlank() && !uiState.isConnecting,
            filled = false,
        )
    }
    Action("HISTORY", onClick = { onAction(HomeAction.OpenHistory) }, filled = false)
}
