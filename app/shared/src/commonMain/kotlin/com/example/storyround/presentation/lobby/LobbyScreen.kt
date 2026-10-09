package com.example.storyround.presentation.lobby

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    if (uiState.isHost) {
        Text("LOBBY SETTINGS", style = MaterialTheme.typography.labelLarge)
        Text("Opening writer: ${uiState.players.firstOrNull { it.id == uiState.openingPlayerId }?.name ?: "Random player"}")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onAction(LobbyAction.OpeningPlayerSelected(null)) }) { Text("RANDOM") }
            uiState.players.forEach { player ->
                OutlinedButton(onClick = { onAction(LobbyAction.OpeningPlayerSelected(player.id)) }) { Text(player.name) }
            }
        }
        Text("Rounds: ${uiState.maxRounds} · each player writes ${uiState.maxRounds} times")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onAction(LobbyAction.RoundsChanged(uiState.maxRounds - 1)) }) { Text("−") }
            OutlinedButton(onClick = { onAction(LobbyAction.RoundsChanged(uiState.maxRounds + 1)) }) { Text("+") }
        }
        Action("START GAME", onClick = { onAction(LobbyAction.StartGame) })
    }
    Action("LEAVE LOBBY", onClick = { onAction(LobbyAction.Leave) }, filled = false)
}
