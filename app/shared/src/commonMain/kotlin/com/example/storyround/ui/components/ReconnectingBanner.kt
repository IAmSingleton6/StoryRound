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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.preview.ComponentPreview
import com.example.storyround.ui.theme.OutlineWidth
import com.example.storyround.ui.theme.StoryRoundTheme
import org.jetbrains.compose.resources.stringResource
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.ic_wifi_off
import storyround.app.shared.generated.resources.reconnecting_banner

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
                .border(OutlineWidth.Thin, colors.ink, ReconnectingBannerShape),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TintedIcon(Res.drawable.ic_wifi_off, tint = colors.ink, modifier = Modifier.size(16.dp))
        BasicText(
            stringResource(Res.string.reconnecting_banner),
            style = StoryRoundTheme.typography.labelSmall.copy(color = colors.ink),
        )
    }
}

@Preview
@Composable
private fun ReconnectingBannerPreview() = ComponentPreview { ReconnectingBanner() }
