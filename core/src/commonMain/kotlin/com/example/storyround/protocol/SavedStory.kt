package com.example.storyround.protocol

import kotlinx.serialization.Serializable

@Serializable
data class SavedStory(
    val id: String,
    val prompt: String,
    val promptPlayerId: String,
    val createdAtEpochMillis: Long,
    val players: List<Player>,
    val contributions: List<Contribution>,
)
