package com.example.storyround.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.example.storyround.presentation.model.ContributionUi
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.ui.theme.StoryRoundInk
import com.example.storyround.ui.theme.toUiColor
import kotlinx.coroutines.delay

private data class StorySegment(
    val text: String,
    val author: PlayerUi?,
)

@Composable
internal fun Story(
    prompt: String,
    promptAuthor: PlayerUi? = null,
    contributions: List<ContributionUi>,
    modifier: Modifier,
    emptyPlaceholder: String? = null,
) {
    val segments =
        remember(prompt, promptAuthor, contributions) {
            buildList {
                prompt.trim().takeIf(String::isNotEmpty)?.let { add(StorySegment(it, promptAuthor)) }
                contributions.forEach { contribution ->
                    contribution.text.trim().takeIf(String::isNotEmpty)?.let { add(StorySegment(it, contribution.player)) }
                }
            }
        }
    val scroll = rememberScrollState()
    LaunchedEffect(segments.size) {
        delay(220)
        scroll.animateScrollTo(scroll.maxValue)
    }
    Column(modifier.verticalScroll(scroll).padding(24.dp)) {
        if (segments.isEmpty()) {
            emptyPlaceholder?.let {
                Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            AnimatedContent(
                targetState = segments,
                transitionSpec = {
                    (fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 4 }) togetherWith fadeOut(tween(90))
                },
                label = "storyContribution",
            ) { visibleSegments ->
                Text(
                    text = visibleSegments.toAnnotatedStory(),
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = visibleSegments.joinToString(" ") { it.text } },
                    style = MaterialTheme.typography.bodyLarge,
                    color = StoryRoundInk,
                )
            }
        }
    }
}

private fun List<StorySegment>.toAnnotatedStory() =
    buildAnnotatedString {
        forEachIndexed { index, segment ->
            if (index > 0) append(' ')
            val start = length
            append(segment.text)
            segment.author?.let { author ->
                addStyle(SpanStyle(background = author.colour.toUiColor().copy(alpha = .26f)), start, length)
            }
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
