package com.example.storyround.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.preview.ComponentPreview
import com.example.storyround.ui.theme.MinTouchTarget
import com.example.storyround.ui.theme.OutlineWidth
import com.example.storyround.ui.theme.StoryRoundTheme
import org.jetbrains.compose.resources.DrawableResource
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.ic_back
import storyround.app.shared.generated.resources.ic_minus
import storyround.app.shared.generated.resources.ic_plus

@Composable
internal fun CircleIconButton(
    icon: DrawableResource,
    contentDescription: String,
    onClick: () -> Unit,
    fill: Color,
    outline: Color,
    tint: Color,
    iconSize: Dp,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier =
            modifier
                .size(MinTouchTarget)
                .background(fill, CircleShape)
                .border(OutlineWidth.Thin, outline, CircleShape)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        TintedIcon(icon, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Preview
@Composable
private fun CircleIconButtonPreview() =
    ComponentPreview {
        val colors = StoryRoundTheme.colors
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CircleIconButton(
                icon = Res.drawable.ic_back,
                contentDescription = "Back",
                onClick = {},
                fill = colors.stageScrim,
                outline = colors.stageOutline,
                tint = colors.onStage,
                iconSize = 20.dp,
            )
            CircleIconButton(
                icon = Res.drawable.ic_plus,
                contentDescription = "More rounds",
                onClick = {},
                fill = colors.paper,
                outline = colors.ink,
                tint = colors.ink,
                iconSize = 18.dp,
            )
            CircleIconButton(
                icon = Res.drawable.ic_minus,
                contentDescription = "Fewer rounds",
                onClick = {},
                fill = colors.stageDisabledFill,
                outline = colors.stageDisabledOutline,
                tint = colors.onStageDisabled,
                iconSize = 18.dp,
                enabled = false,
            )
        }
    }
