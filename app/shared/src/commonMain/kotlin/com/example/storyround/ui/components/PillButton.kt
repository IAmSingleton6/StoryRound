package com.example.storyround.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.preview.ComponentPreview
import com.example.storyround.ui.theme.MinTouchTarget
import com.example.storyround.ui.theme.OutlineWidth
import com.example.storyround.ui.theme.StoryRoundTheme
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.ic_back
import storyround.app.shared.generated.resources.ic_book

private val PillShape = RoundedCornerShape(percent = 50)

@Composable
internal fun PillButton(
    onClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    spacing: Dp = 8.dp,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = StoryRoundTheme.colors
    Row(
        modifier =
            modifier
                .height(MinTouchTarget)
                .background(colors.stageScrim, PillShape)
                .border(OutlineWidth.Thin, colors.stageOutline, PillShape)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Preview
@Composable
private fun PillButtonPreview() =
    ComponentPreview {
        val colors = StoryRoundTheme.colors
        PillButton(onClick = {}, contentPadding = PaddingValues(start = 10.dp, end = 16.dp), spacing = 6.dp) {
            TintedIcon(Res.drawable.ic_back, tint = colors.onStage, modifier = Modifier.size(18.dp))
            BasicText("Leave lobby", style = StoryRoundTheme.typography.labelMedium.copy(color = colors.onStage))
        }
        PillButton(onClick = {}, contentPadding = PaddingValues(horizontal = 13.dp)) {
            TintedIcon(Res.drawable.ic_book, tint = colors.onStage, modifier = Modifier.size(18.dp))
        }
    }
