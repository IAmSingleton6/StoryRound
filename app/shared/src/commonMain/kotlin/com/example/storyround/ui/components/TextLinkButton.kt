package com.example.storyround.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.example.storyround.ui.preview.ComponentPreview
import com.example.storyround.ui.preview.PreviewSurface
import com.example.storyround.ui.theme.MinTouchTarget
import com.example.storyround.ui.theme.StoryRoundTheme

@Composable
internal fun TextLinkButton(
    label: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .height(MinTouchTarget)
                .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(label, style = StoryRoundTheme.typography.link.copy(color = color))
    }
}

@Preview
@Composable
private fun TextLinkButtonPreview() =
    ComponentPreview(PreviewSurface.Paper) {
        TextLinkButton("Cancel", onClick = {}, color = StoryRoundTheme.colors.ink, modifier = Modifier.fillMaxWidth())
    }
