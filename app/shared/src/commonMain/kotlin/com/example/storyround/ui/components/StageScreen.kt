package com.example.storyround.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.preview.PhonePreview
import com.example.storyround.ui.preview.ScreenPreview

internal val RoomScreenPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 24.dp)

internal val StoryScreenPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)

private val ReconnectingBannerPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)

@Composable
internal fun StageScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    isReconnecting: Boolean = false,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    overlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) = StageBackground(modifier) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(contentPadding),
        verticalArrangement = verticalArrangement,
        content = content,
    )
    overlay()
    if (isReconnecting) {
        ReconnectingBanner(
            Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(ReconnectingBannerPadding),
        )
    }
}

@PhonePreview
@Composable
private fun StageScreenReconnectingPreview() =
    ScreenPreview {
        StageScreen(contentPadding = RoomScreenPadding, isReconnecting = true) {
            StoryPaper(Modifier.fillMaxWidth().height(240.dp)) {}
        }
    }
