package com.example.storyround.presentation.lobby

import com.example.storyround.presentation.model.PlayerUi

internal data class LobbyUiState(
    val roomCode: String = "",
    val players: List<PlayerUi> = emptyList(),
    val maxPlayers: Int = 0,
    val hostId: String = "",
    val currentPlayerId: String? = null,
    val openingPlayerId: String? = null,
    val maxRounds: Int = 0,
    val canDecreaseRounds: Boolean = false,
    val canIncreaseRounds: Boolean = false,
    val totalTurns: Int = 0,
    val oneWordPerTurn: Boolean = false,
    val isReconnecting: Boolean = false,
) {
    val isHost: Boolean
        get() = currentPlayerId != null && currentPlayerId == hostId

    val host: PlayerUi?
        get() = players.firstOrNull { it.id == hostId }

    val openingPlayer: PlayerUi?
        get() = players.firstOrNull { it.id == openingPlayerId }

    val openSeats: Int
        get() = (maxPlayers - players.size).coerceAtLeast(0)

    val seats: List<LobbySeat>
        get() {
            val taken =
                players.map { player ->
                    LobbySeat.Taken(player, isHost = player.id == hostId, isCurrentPlayer = player.id == currentPlayerId)
                }
            return if (openSeats > 0) taken + LobbySeat.Open(openSeats) else taken
        }
}

internal sealed interface LobbySeat {
    data class Taken(
        val player: PlayerUi,
        val isHost: Boolean,
        val isCurrentPlayer: Boolean,
    ) : LobbySeat

    data class Open(
        val count: Int,
    ) : LobbySeat
}
