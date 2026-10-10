package com.example.storyround.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.theme.StoryRoundTheme

private val StoryPaperShape = RoundedCornerShape(20.dp)

@Composable
internal fun StoryPaper(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = StoryRoundTheme.colors
    Box(
        modifier =
            modifier
                .hardShadow(offsetY = 6.dp, color = colors.inkShadow, shape = StoryPaperShape)
                .background(colors.paper, StoryPaperShape)
                .border(2.5.dp, colors.ink, StoryPaperShape)
                .clip(StoryPaperShape),
        content = content,
    )
}
