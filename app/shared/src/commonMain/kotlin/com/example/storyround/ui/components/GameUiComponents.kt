package com.example.storyround.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.ui.theme.StoryRoundInk
import com.example.storyround.ui.theme.toUiColor

@Composable
internal fun Page(content: @Composable ColumnScope.() -> Unit) =
    Column(
        modifier =
            Modifier
                .safeContentPadding()
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )

@Composable
internal fun Player(player: PlayerUi) =
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .background(player.colour.toUiColor(), RoundedCornerShape(50))
                .width(12.dp)
                .height(12.dp),
        )
        Spacer(Modifier.width(5.dp))
        Text(player.name, fontWeight = FontWeight.SemiBold)
    }

@Composable
internal fun Action(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    filled: Boolean = true,
) = Button(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth(),
    enabled = enabled,
    colors =
        if (filled) {
            ButtonDefaults.buttonColors(containerColor = StoryRoundInk)
        } else {
            ButtonDefaults.outlinedButtonColors(contentColor = StoryRoundInk)
        },
) {
    Text(label)
}
