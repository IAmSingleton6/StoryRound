package com.example.storyround.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.preview.ComponentPreview
import com.example.storyround.ui.theme.StoryRoundColors
import com.example.storyround.ui.theme.StoryRoundTheme
import org.jetbrains.compose.resources.DrawableResource
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.ic_replay
import storyround.app.shared.generated.resources.ic_send

private val NoPadding = PaddingValues(0.dp)
private val PressedShadow = 1.dp

@Composable
internal fun RaisedButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: RaisedButtonColor = RaisedButtonColor.Cue,
    size: RaisedButtonSize = RaisedButtonSize.Large,
    enabled: Boolean = true,
    shadowColor: Color = StoryRoundTheme.colors.inkShadow,
    leadingIcon: DrawableResource? = null,
    trailingIcon: DrawableResource? = null,
    contentPadding: PaddingValues = NoPadding,
    textStyle: TextStyle = StoryRoundTheme.typography.buttonLarge,
) {
    val colors = StoryRoundTheme.colors
    val shape = RoundedCornerShape(size.cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressed = enabled && isPressed
    val shadowOffset = if (pressed) PressedShadow else size.shadow
    val pressOffset = size.shadow - shadowOffset
    val contentColor = if (enabled) colors.ink else colors.onPaperMuted
    Row(
        modifier =
            modifier
                .height(size.height)
                .offset(y = pressOffset)
                .printedSurface(
                    fill = if (enabled) color.containerColor(colors) else colors.disabledFill,
                    ink = colors.ink,
                    shape = shape,
                    shadowOffset = shadowOffset,
                    shadowColor = shadowColor,
                ).clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                ).padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(if (leadingIcon != null) 10.dp else 8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingIcon?.let { TintedIcon(it, tint = contentColor, modifier = Modifier.size(20.dp)) }
        BasicText(text = label, style = textStyle.copy(color = contentColor))
        trailingIcon?.let { TintedIcon(it, tint = contentColor, modifier = Modifier.size(18.dp)) }
    }
}

private fun RaisedButtonColor.containerColor(colors: StoryRoundColors): Color =
    when (this) {
        RaisedButtonColor.Cue -> colors.cue
        RaisedButtonColor.Paper -> colors.paper
        RaisedButtonColor.White -> colors.paperWhite
    }

@Preview
@Composable
private fun RaisedButtonPreview() =
    ComponentPreview {
        val typography = StoryRoundTheme.typography
        RaisedButton(label = "START GAME", onClick = {}, modifier = Modifier.fillMaxWidth())
        RaisedButton(label = "PLAY AGAIN", onClick = {}, modifier = Modifier.fillMaxWidth(), leadingIcon = Res.drawable.ic_replay)
        RaisedButton(label = "JOIN GAME", onClick = {}, modifier = Modifier.fillMaxWidth(), color = RaisedButtonColor.White)
        RaisedButton(label = "CREATE GAME", onClick = {}, modifier = Modifier.fillMaxWidth(), enabled = false)
        RaisedButton(
            label = "BACK",
            onClick = {},
            color = RaisedButtonColor.Paper,
            size = RaisedButtonSize.Medium,
            contentPadding = PaddingValues(horizontal = 32.dp),
            textStyle = typography.buttonSmall,
        )
        RaisedButton(
            label = "SUBMIT",
            onClick = {},
            size = RaisedButtonSize.Compact,
            trailingIcon = Res.drawable.ic_send,
            contentPadding = PaddingValues(start = 20.dp, end = 16.dp),
            textStyle = typography.buttonSmall,
        )
    }
