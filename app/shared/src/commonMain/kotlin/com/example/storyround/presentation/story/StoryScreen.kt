package com.example.storyround.presentation.story

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.storyround.presentation.model.MAX_CONTRIBUTION_LENGTH
import com.example.storyround.presentation.model.RoomUi
import com.example.storyround.presentation.model.TurnUi
import com.example.storyround.ui.components.Action
import com.example.storyround.ui.components.Player
import com.example.storyround.ui.components.Story
import com.example.storyround.ui.theme.toUiColor

@Composable
internal fun StoryScreen(
    uiState: StoryUiState,
    onAction: (StoryAction) -> Unit,
) {
    when (uiState) {
        StoryUiState.Loading -> Unit
        is StoryUiState.OpeningPrompt -> OpeningPromptContent(uiState, onAction)
        is StoryUiState.Playing -> PlayingContent(uiState, onAction)
    }
}

@Composable
private fun PlayingContent(
    uiState: StoryUiState.Playing,
    onAction: (StoryAction) -> Unit,
) {
    val turn = uiState.room.turn ?: return
    val isYourTurn = turn.player.id == uiState.playerId

    Column(Modifier.safeContentPadding().fillMaxSize()) {
        StoryHeader(uiState.room)
        TurnHeader(turn, uiState.nowEpochMillis, isYourTurn)
        Story(uiState.room.prompt, uiState.room.promptAuthor, uiState.room.contributions, Modifier.weight(1f).fillMaxWidth())
        ContributionInput(uiState, isYourTurn, turn, onAction)
    }
}

@Composable
private fun StoryHeader(room: RoomUi) {
    Row(
        modifier = Modifier.padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        room.players.forEach { Player(it) }
    }
}

@Composable
private fun TurnHeader(
    turn: TurnUi,
    nowEpochMillis: Long,
    isYourTurn: Boolean,
) {
    val seconds = ((turn.deadlineEpochMillis - nowEpochMillis).coerceAtLeast(0) / 1000.0)
    Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            if (isYourTurn) "YOUR TURN" else "${turn.player.name.uppercase()}'S TURN",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = turn.player.colour.toUiColor(),
        )
        Text("${seconds.toString().take(4)}s", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ContributionInput(
    uiState: StoryUiState.Playing,
    isYourTurn: Boolean,
    turn: TurnUi,
    onAction: (StoryAction) -> Unit,
) {
    Column(Modifier.imePadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = uiState.contributionDraft,
            onValueChange = { onAction(StoryAction.ContributionChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = isYourTurn,
            placeholder = { Text(if (isYourTurn) "Continue the story..." else "Waiting for ${turn.player.name}...") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("${uiState.contributionDraft.length}/$MAX_CONTRIBUTION_LENGTH", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Action(
                "SUBMIT",
                onClick = { onAction(StoryAction.SubmitContribution) },
                enabled = isYourTurn && uiState.contributionDraft.isNotBlank(),
            )
        }
    }
}
