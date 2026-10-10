package com.example.storyround.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.theme.StoryRoundColors
import com.example.storyround.ui.theme.StoryRoundTheme

private val SrButtonShape = RoundedCornerShape(16.dp)

@Composable
internal fun SrButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SrButtonVariant = SrButtonVariant.Cue,
    enabled: Boolean = true,
    shadowColor: Color = StoryRoundTheme.colors.inkShadow,
) {
    val colors = StoryRoundTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressed = enabled && isPressed
    Box(
        modifier =
            modifier
                .height(60.dp)
                .offset(y = if (pressed) 4.dp else 0.dp)
                .hardShadow(offsetY = if (pressed) 1.dp else 5.dp, color = shadowColor, shape = SrButtonShape)
                .background(if (enabled) variant.containerColor(colors) else colors.disabledFill, SrButtonShape)
                .border(2.5.dp, colors.ink, SrButtonShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = StoryRoundTheme.typography.button.copy(color = if (enabled) colors.ink else colors.onPaperMuted),
        )
    }
}

private fun SrButtonVariant.containerColor(colors: StoryRoundColors): Color =
    when (this) {
        SrButtonVariant.Cue -> colors.cue
        SrButtonVariant.Paper -> colors.paper
        SrButtonVariant.White -> colors.paperWhite
    }
