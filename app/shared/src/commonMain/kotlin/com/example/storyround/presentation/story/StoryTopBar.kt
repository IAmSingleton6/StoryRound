package com.example.storyround.presentation.story

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.ui.components.PlayerStrip
import com.example.storyround.ui.preview.ComponentPreview
import com.example.storyround.ui.preview.PreviewPlayers
import com.example.storyround.ui.theme.StoryRoundTheme

@Composable
internal fun StoryTopBar(
    label: String,
    detail: String,
    players: List<PlayerUi>,
    activePlayerId: String?,
) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth().height(40.dp).padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            BasicText(label, style = typography.eyebrowSmall.copy(color = colors.onStageMuted))
            BasicText(detail, style = typography.labelMedium.copy(color = colors.onStage))
        }
        PlayerStrip(players, activePlayerId)
    }
}

@Preview
@Composable
private fun StoryTopBarPreview() =
    ComponentPreview {
        StoryTopBar(label = "ROUND 2 OF 2", detail = "Turn 5 of 8", players = PreviewPlayers.all, activePlayerId = PreviewPlayers.alex.id)
    }
