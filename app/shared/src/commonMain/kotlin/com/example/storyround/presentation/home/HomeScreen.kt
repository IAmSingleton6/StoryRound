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
        "Create a room or join your friends with a room code.",
        style = MaterialTheme.typography.titleMedium,
    )
    OutlinedTextField(
        value = uiState.playerName,
        onValueChange = { onAction(HomeAction.PlayerNameChanged(it)) },
        label = { Text("Your name") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    OutlinedTextField(
        value = uiState.roomCode,
        onValueChange = { onAction(HomeAction.RoomCodeChanged(it)) },
        label = { Text("Room code (leave blank to create)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    Spacer(Modifier.weight(1f))
    Action(
        label = if (uiState.roomCode.isBlank()) "CREATE GAME" else "JOIN GAME",
        onClick = { onAction(HomeAction.Submit) },
        enabled = uiState.playerName.isNotBlank() && !uiState.isConnecting,
    )
    Action("HISTORY", onClick = { onAction(HomeAction.OpenHistory) }, filled = false)
}
