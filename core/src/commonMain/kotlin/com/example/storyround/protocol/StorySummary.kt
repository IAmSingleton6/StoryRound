package com.example.storyround.protocol

import kotlinx.serialization.Serializable

@Serializable
data class StorySummary(
    val id: String,
    val prompt: String,
    val createdAtEpochMillis: Long,
    val playerCount: Int = 0,
    val contributionCount: Int,
)
