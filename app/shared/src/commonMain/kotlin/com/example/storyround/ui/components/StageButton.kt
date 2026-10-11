package com.example.storyround.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.theme.StoryRoundTheme

private val StageButtonShape = RoundedCornerShape(14.dp)

@Composable
internal fun StageButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = StoryRoundTheme.colors
    Box(
        modifier =
            modifier
                .height(52.dp)
                .background(colors.stageRaised, StageButtonShape)
                .border(2.dp, if (enabled) colors.stageOutline else colors.stageDisabledOutline, StageButtonShape)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(label, style = StoryRoundTheme.typography.pill.copy(color = if (enabled) colors.onStage else colors.onStageDisabled))
    }
}
