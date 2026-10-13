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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.theme.StoryRoundTheme

private val PillShape = RoundedCornerShape(22.dp)

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
                .height(44.dp)
                .background(colors.stageScrim, PillShape)
                .border(2.dp, colors.stageOutline, PillShape)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
