package com.example.storyround.room

import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.RoomSettings

internal class MutableGameRoom(
    val code: String,
    var hostId: String,
    val players: MutableList<Player>,
    val maxPlayers: Int,
    var status: GameStatus = GameStatus.Lobby,
    val sessionTokens: MutableMap<String, String> = mutableMapOf(),
    var prompt: String? = null,
    var openingPlayerId: String? = null,
    val contributions: MutableList<Contribution> = mutableListOf(),
    var currentPlayerId: String? = null,
    var turnNumber: Int = 0,
    var turnDeadlineEpochMillis: Long? = null,
    var settings: RoomSettings,
)
