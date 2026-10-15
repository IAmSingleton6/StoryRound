package com.example.storyround.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.preview.ComponentPreview
import com.example.storyround.ui.theme.StoryRoundTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.ic_alert
import storyround.app.shared.generated.resources.ic_back
import storyround.app.shared.generated.resources.ic_book
import storyround.app.shared.generated.resources.ic_dice
import storyround.app.shared.generated.resources.ic_feather
import storyround.app.shared.generated.resources.ic_minus
import storyround.app.shared.generated.resources.ic_plus
import storyround.app.shared.generated.resources.ic_replay
import storyround.app.shared.generated.resources.ic_send
import storyround.app.shared.generated.resources.ic_wifi_off

@Composable
internal fun TintedIcon(
    icon: DrawableResource,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(icon),
        contentDescription = null,
        modifier = modifier,
        colorFilter = ColorFilter.tint(tint),
    )
}

@Preview
@Composable
private fun TintedIconPreview() =
    ComponentPreview {
        val icons =
            listOf(
                Res.drawable.ic_alert,
                Res.drawable.ic_back,
                Res.drawable.ic_book,
                Res.drawable.ic_dice,
                Res.drawable.ic_feather,
                Res.drawable.ic_minus,
                Res.drawable.ic_plus,
                Res.drawable.ic_replay,
                Res.drawable.ic_send,
                Res.drawable.ic_wifi_off,
            )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            icons.forEach { icon -> TintedIcon(icon, tint = StoryRoundTheme.colors.onStage, modifier = Modifier.size(24.dp)) }
        }
    }
