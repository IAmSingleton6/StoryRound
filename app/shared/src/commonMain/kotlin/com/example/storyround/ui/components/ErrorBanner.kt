package com.example.storyround.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.preview.ComponentPreview
import com.example.storyround.ui.theme.OutlineWidth
import com.example.storyround.ui.theme.StoryRoundTheme
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.ic_alert

private val ErrorBannerShape = RoundedCornerShape(14.dp)

@Composable
internal fun ErrorBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    val colors = StoryRoundTheme.colors
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .semantics { liveRegion = LiveRegionMode.Assertive }
                .background(colors.coral, ErrorBannerShape)
                .border(OutlineWidth.Regular, colors.ink, ErrorBannerShape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        TintedIcon(Res.drawable.ic_alert, tint = colors.ink, modifier = Modifier.padding(top = 1.dp).size(18.dp))
        BasicText(message, style = StoryRoundTheme.typography.labelSmall.copy(color = colors.ink))
    }
}

@Preview
@Composable
private fun ErrorBannerPreview() =
    ComponentPreview {
        ErrorBanner("Can't reach Story Round right now. Check your internet connection and try again.")
    }
