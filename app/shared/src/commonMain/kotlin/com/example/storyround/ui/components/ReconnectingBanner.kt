package com.example.storyround.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

private val ReconnectingBannerShape = RoundedCornerShape(17.dp)

@Composable
internal fun ReconnectingBanner(modifier: Modifier = Modifier) {
    val colors = StoryRoundTheme.colors
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(34.dp)
                .semantics { liveRegion = LiveRegionMode.Polite }
                .background(colors.marigold, ReconnectingBannerShape)
                .border(2.dp, colors.ink, ReconnectingBannerShape),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TintedIcon(StoryRoundIcons.WifiOff, tint = colors.ink, modifier = Modifier.size(16.dp))
        BasicText(
            "Reconnecting… your seat is saved",
            style = StoryRoundTheme.typography.callout.copy(color = colors.ink),
        )
    }
}
