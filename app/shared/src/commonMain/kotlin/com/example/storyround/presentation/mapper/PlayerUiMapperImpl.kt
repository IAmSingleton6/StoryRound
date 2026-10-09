package com.example.storyround.presentation.mapper

import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.protocol.Player

internal class PlayerUiMapperImpl : PlayerUiMapper {
    override fun map(player: Player): PlayerUi =
        PlayerUi(
            id = player.id,
            name = player.name,
            colour = player.colour,
        )
}
