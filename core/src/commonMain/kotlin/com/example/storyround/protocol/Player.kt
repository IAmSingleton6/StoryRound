package com.example.storyround.protocol

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: String,
    val name: String,
    val colour: PlayerColour,
)
