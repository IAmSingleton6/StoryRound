package com.example.storyround.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.theme.StoryRoundTheme

private val ComponentPreviewWidth = 390.dp

@Preview(widthDp = 390, heightDp = 844)
internal annotation class PhonePreview

internal enum class PreviewSurface {
    Stage,
    Paper,
}

@Composable
internal fun ComponentPreview(
    surface: PreviewSurface = PreviewSurface.Stage,
    content: @Composable ColumnScope.() -> Unit,
) = StoryRoundTheme {
    val colors = StoryRoundTheme.colors
    val background =
        when (surface) {
            PreviewSurface.Stage -> colors.stage
            PreviewSurface.Paper -> colors.paper
        }
    Column(
        modifier = Modifier.width(ComponentPreviewWidth).background(background).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

@Composable
internal fun ScreenPreview(content: @Composable () -> Unit) = StoryRoundTheme(content)
