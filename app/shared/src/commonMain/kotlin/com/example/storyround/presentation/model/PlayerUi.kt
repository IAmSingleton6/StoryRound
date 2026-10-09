package com.example.storyround.presentation.model

import com.example.storyround.protocol.PlayerColour

internal data class PlayerUi(
    val id: String,
    val name: String,
    val colour: PlayerColour,
)
