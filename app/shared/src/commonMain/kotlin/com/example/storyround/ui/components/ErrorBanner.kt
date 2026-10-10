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
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.theme.StoryRoundTheme

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
                .border(2.5.dp, colors.ink, ErrorBannerShape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        SrIcon(SrIcons.Alert, tint = colors.ink, modifier = Modifier.padding(top = 1.dp).size(18.dp))
        BasicText(message, style = StoryRoundTheme.typography.callout.copy(color = colors.ink))
    }
}
