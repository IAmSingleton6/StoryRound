package com.example.storyround.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ClientMessage {
    @Serializable
    @SerialName("CREATE_ROOM")
    data class CreateRoom(
        val playerName: String,
    ) : ClientMessage

    @Serializable
    @SerialName("JOIN_ROOM")
    data class JoinRoom(
        val roomCode: String,
        val playerName: String,
    ) : ClientMessage

    @Serializable
    @SerialName("START_GAME")
    data class StartGame(
        val roomCode: String,
        val playerId: String,
        val openingPlayerId: String? = null,
        val maxRounds: Int,
    ) : ClientMessage

    @Serializable
    @SerialName("LEAVE_ROOM")
    data class LeaveRoom(
        val roomCode: String,
        val playerId: String,
    ) : ClientMessage

    @Serializable
    @SerialName("SUBMIT_PROMPT")
    data class SubmitPrompt(
        val roomCode: String,
        val playerId: String,
        val text: String,
    ) : ClientMessage

    @Serializable
    @SerialName("SUBMIT_CONTRIBUTION")
    data class SubmitContribution(
        val roomCode: String,
        val playerId: String,
        val text: String,
    ) : ClientMessage

    @Serializable
    @SerialName("PLAY_AGAIN")
    data class PlayAgain(
        val roomCode: String,
        val playerId: String,
    ) : ClientMessage

    @Serializable
    @SerialName("RECONNECT")
    data class Reconnect(
        val roomCode: String,
        val playerId: String,
        val sessionToken: String,
    ) : ClientMessage
}

@Serializable
sealed interface ServerMessage {
    @Serializable
    @SerialName("ROOM_STATE")
    data class RoomStateChanged(
        val room: RoomState,
    ) : ServerMessage

    @Serializable
    @SerialName("PLAYER_JOINED")
    data class PlayerJoined(
        val player: Player,
    ) : ServerMessage

    @Serializable
    @SerialName("PLAYER_LEFT")
    data class PlayerLeft(
        val playerId: String,
    ) : ServerMessage

    @Serializable
    @SerialName("SESSION_ESTABLISHED")
    data class SessionEstablished(
        val playerId: String,
        val sessionToken: String,
    ) : ServerMessage

    @Serializable
    @SerialName("TURN_STARTED")
    data class TurnStarted(
        val playerId: String,
        val deadlineEpochMillis: Long,
    ) : ServerMessage

    @Serializable
    @SerialName("CONTRIBUTION_ADDED")
    data class ContributionAdded(
        val contribution: Contribution,
    ) : ServerMessage

    @Serializable
    @SerialName("TURN_EXPIRED")
    data class TurnExpired(
        val playerId: String,
    ) : ServerMessage

    @Serializable
    @SerialName("GAME_FINISHED")
    data class GameFinished(
        val room: RoomState,
        val storyId: String? = null,
    ) : ServerMessage

    @Serializable
    @SerialName("ERROR")
    data class Error(
        val code: String,
        val message: String,
    ) : ServerMessage
}
