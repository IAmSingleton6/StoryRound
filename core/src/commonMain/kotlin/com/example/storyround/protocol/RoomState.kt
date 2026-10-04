package com.example.storyround.protocol

import kotlinx.serialization.Serializable

@Serializable
data class RoomState(
    val roomCode: String,
    val hostId: String,
    val players: List<Player>,
    val status: GameStatus,
    val prompt: String? = null,
    val contributions: List<Contribution> = emptyList(),
    val currentPlayerId: String? = null,
    val turnNumber: Int = 0,
    val turnDeadlineEpochMillis: Long? = null,
    val maxPlayers: Int = 10,
)
