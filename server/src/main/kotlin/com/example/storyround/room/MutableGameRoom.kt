package com.example.storyround.room

import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.RoomState

internal class MutableGameRoom(
    val code: String,
    val hostId: String,
    val players: MutableList<Player>,
    val maxPlayers: Int,
    var status: GameStatus = GameStatus.Lobby,
    val sessionTokens: MutableMap<String, String> = mutableMapOf(),
    var prompt: String? = null,
    val contributions: MutableList<Contribution> = mutableListOf(),
    var currentPlayerId: String? = null,
    var turnNumber: Int = 0,
    var turnDeadlineEpochMillis: Long? = null,
) {
    fun toState() =
        RoomState(
            roomCode = code,
            hostId = hostId,
            players = players.toList(),
            status = status,
            prompt = prompt,
            contributions = contributions.toList(),
            currentPlayerId = currentPlayerId,
            turnNumber = turnNumber,
            turnDeadlineEpochMillis = turnDeadlineEpochMillis,
            maxPlayers = maxPlayers,
        )
}
