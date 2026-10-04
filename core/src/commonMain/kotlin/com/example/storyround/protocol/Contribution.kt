package com.example.storyround.protocol

import kotlinx.serialization.Serializable

@Serializable
data class Contribution(
    val playerId: String,
    val text: String,
    val turnNumber: Int,
)
