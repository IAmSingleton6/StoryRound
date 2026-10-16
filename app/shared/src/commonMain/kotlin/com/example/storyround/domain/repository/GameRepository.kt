package com.example.storyround.domain.repository

import com.example.storyround.domain.model.GameState
import com.example.storyround.protocol.RoomSettings
import kotlinx.coroutines.flow.StateFlow

internal interface GameRepository {
    val state: StateFlow<GameState>

    suspend fun connect()

    suspend fun createRoom(playerName: String)

    suspend fun joinRoom(
        roomCode: String,
        playerName: String,
    )

    suspend fun startGame(
        openingPlayerId: String?,
        settings: RoomSettings,
    )

    suspend fun submitPrompt(text: String)

    suspend fun submitContribution(text: String)

    suspend fun playAgain()

    suspend fun leaveRoom()

    fun disconnect()
}
