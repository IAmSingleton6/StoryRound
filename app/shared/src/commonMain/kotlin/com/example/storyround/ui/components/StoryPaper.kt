package com.example.storyround.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.preview.ComponentPreview
import com.example.storyround.ui.theme.CardShape
import com.example.storyround.ui.theme.StoryRoundTheme

@Composable
internal fun StoryPaper(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = StoryRoundTheme.colors
    Box(
        modifier =
            modifier
                .printedSurface(
                    fill = colors.paper,
                    ink = colors.ink,
                    shape = CardShape,
                    shadowOffset = 6.dp,
                    shadowColor = colors.inkShadow,
                ).clip(CardShape),
        content = content,
    )
}

@Preview
@Composable
private fun StoryPaperPreview() = ComponentPreview { StoryPaper(Modifier.fillMaxWidth().height(160.dp)) {} }
