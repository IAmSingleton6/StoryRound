package com.example.storyround.room

import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.Player

internal sealed interface RoomEvent {
    data class RoomCreated(
        val host: Player,
        val sessionToken: String,
    ) : RoomEvent

    data class PlayerJoined(
        val player: Player,
        val sessionToken: String,
    ) : RoomEvent

    data class PlayerReconnected(
        val player: Player,
        val sessionToken: String,
    ) : RoomEvent

    data class PlayerLeft(
        val playerId: String,
    ) : RoomEvent

    data object GameStarted : RoomEvent

    data class PromptSubmitted(
        val firstTurn: TurnOutcome.NextTurn,
    ) : RoomEvent

    data class ContributionAdded(
        val contribution: Contribution,
        val outcome: TurnOutcome,
    ) : RoomEvent

    data class TurnExpired(
        val playerId: String,
        val outcome: TurnOutcome,
    ) : RoomEvent

    data object GameReset : RoomEvent

    data object RoomClosed : RoomEvent
}
