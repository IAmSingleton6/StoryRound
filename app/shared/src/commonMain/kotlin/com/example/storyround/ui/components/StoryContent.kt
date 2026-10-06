package com.example.storyround.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.storyround.presentation.model.ContributionUi
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.ui.theme.StoryRoundInk
import com.example.storyround.ui.theme.toUiColor
import kotlinx.coroutines.delay

@Composable
internal fun Story(
    prompt: String,
    contributions: List<ContributionUi>,
    modifier: Modifier,
) {
    val scroll = rememberScrollState()
    LaunchedEffect(contributions.size) {
        delay(220)
        scroll.animateScrollTo(scroll.maxValue)
    }
    Column(
        modifier.verticalScroll(scroll).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(prompt, style = MaterialTheme.typography.bodyLarge, color = StoryRoundInk)
        contributions.forEachIndexed { index, contribution ->
            AnimatedContribution(contribution, isNewest = index == contributions.lastIndex)
        }
    }
}

@Composable
private fun AnimatedContribution(
    contribution: ContributionUi,
    isNewest: Boolean,
) {
    var visible by remember(contribution.order) { mutableStateOf(!isNewest) }
    LaunchedEffect(contribution.order, isNewest) {
        if (isNewest) visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220)) + expandVertically(tween(220)),
    ) {
        Text(
            contribution.text,
            Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "${contribution.player.name}'s contribution: ${contribution.text}" }
                .background(
                    contribution.player.colour
                        .toUiColor()
                        .copy(alpha = .22f),
                    RoundedCornerShape(8.dp),
                ).padding(12.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
internal fun StoryPlayers(players: List<PlayerUi>) {
    Text("PLAYERS", style = MaterialTheme.typography.labelLarge)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        players.forEach { player -> Player(player) }
    }
}
