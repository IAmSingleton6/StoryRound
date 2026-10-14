package com.example.storyround.room

import com.example.storyround.protocol.RoomSettings

internal interface RoomRegistry {
    fun createRoom(playerName: String): RegistryResult

    fun joinRoom(
        roomCode: String,
        playerName: String,
    ): RegistryResult

    fun reconnect(
        roomCode: String,
        playerId: String,
        sessionToken: String,
    ): RegistryResult

    fun startGame(
        roomCode: String,
        playerId: String,
        openingPlayerId: String? = playerId,
        settings: RoomSettings,
    ): RegistryResult

    fun leaveRoom(
        roomCode: String,
        playerId: String,
    ): RegistryResult

    fun submitPrompt(
        roomCode: String,
        playerId: String,
        text: String,
    ): RegistryResult

    fun submitContribution(
        roomCode: String,
        playerId: String,
        text: String,
    ): RegistryResult

    fun playAgain(
        roomCode: String,
        playerId: String,
    ): RegistryResult

    fun expireTurn(
        roomCode: String,
        deadlineEpochMillis: Long,
    ): RegistryResult.Success?
}
