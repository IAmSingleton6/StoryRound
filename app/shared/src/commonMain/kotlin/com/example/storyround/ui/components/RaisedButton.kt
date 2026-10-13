package com.example.storyround.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.theme.StoryRoundColors
import com.example.storyround.ui.theme.StoryRoundTheme

private val NoPadding = PaddingValues(0.dp)

@Composable
internal fun RaisedButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: RaisedButtonColor = RaisedButtonColor.Cue,
    size: RaisedButtonSize = RaisedButtonSize.Large,
    enabled: Boolean = true,
    shadowColor: Color = StoryRoundTheme.colors.inkShadow,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    contentPadding: PaddingValues = NoPadding,
    textStyle: TextStyle = StoryRoundTheme.typography.button,
) {
    val colors = StoryRoundTheme.colors
    val shape = RoundedCornerShape(size.cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressed = enabled && isPressed
    val content = if (enabled) colors.ink else colors.onPaperMuted
    Row(
        modifier =
            modifier
                .height(size.height)
                .offset(y = if (pressed) size.shadow - 1.dp else 0.dp)
                .hardShadow(offsetY = if (pressed) 1.dp else size.shadow, color = shadowColor, shape = shape)
                .background(if (enabled) color.containerColor(colors) else colors.disabledFill, shape)
                .border(2.5.dp, colors.ink, shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                ).padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(if (leadingIcon != null) 10.dp else 8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingIcon?.let { TintedIcon(it, tint = content, modifier = Modifier.size(20.dp)) }
        BasicText(text = label, style = textStyle.copy(color = content))
        trailingIcon?.let { TintedIcon(it, tint = content, modifier = Modifier.size(18.dp)) }
    }
}

private fun RaisedButtonColor.containerColor(colors: StoryRoundColors): Color =
    when (this) {
        RaisedButtonColor.Cue -> colors.cue
        RaisedButtonColor.Paper -> colors.paper
        RaisedButtonColor.White -> colors.paperWhite
    }
