package com.example.storyround.presentation.lobby

import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.storyround.ui.components.Action
import com.example.storyround.ui.components.Page
import com.example.storyround.ui.components.Player

@Composable
internal fun LobbyScreen(
    uiState: LobbyUiState,
    onAction: (LobbyAction) -> Unit,
) = Page {
    Text("STORY RELAY", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
    Text("ROOM CODE", style = MaterialTheme.typography.labelLarge)
    Text(uiState.roomCode, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black)
    Text("PLAYERS", style = MaterialTheme.typography.labelLarge)
    uiState.players.forEach { player -> Player(player) }
    Text("${uiState.players.size} / ${uiState.maxPlayers} PLAYERS", color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.weight(1f))
    Text(if (uiState.isHost) "You are the host. Start when you're ready." else "Waiting for the host to start the game...")
    if (uiState.isHost) Action("START GAME", onClick = { onAction(LobbyAction.StartGame) })
    Action("LEAVE", onClick = { onAction(LobbyAction.Leave) }, filled = false)
}
