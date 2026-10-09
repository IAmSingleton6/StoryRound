package com.example.storyround.presentation.mapper

import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.protocol.Player

internal interface PlayerUiMapper {
    fun map(player: Player): PlayerUi
}
