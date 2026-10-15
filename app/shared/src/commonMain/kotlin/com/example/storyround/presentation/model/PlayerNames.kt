package com.example.storyround.presentation.model

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.player_legend_you
import storyround.app.shared.generated.resources.player_you
import storyround.app.shared.generated.resources.player_you_mid_sentence

@Composable
internal fun PlayerUi.nameFor(viewerId: String?): String = if (id == viewerId) stringResource(Res.string.player_you) else name

@Composable
internal fun PlayerUi.midSentenceNameFor(viewerId: String?): String =
    if (id == viewerId) stringResource(Res.string.player_you_mid_sentence) else name

@Composable
internal fun PlayerUi.legendNameFor(viewerId: String?): String =
    if (id == viewerId) stringResource(Res.string.player_legend_you, name) else name
