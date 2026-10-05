package com.example.storyround.domain.repository

import com.example.storyround.domain.model.RemoteGameState
import kotlinx.coroutines.flow.StateFlow

internal interface GameRepository {
    val state: StateFlow<RemoteGameState>

    suspend fun connect()

    suspend fun createRoom(playerName: String)

    suspend fun joinRoom(
        roomCode: String,
        playerName: String,
    )

    suspend fun startGame()

    suspend fun submitPrompt(text: String)

    suspend fun submitContribution(text: String)

    suspend fun playAgain()

    suspend fun loadHistory()

    suspend fun openStory(id: String): Boolean

    fun closeStory()

    fun disconnect()

    fun close()
}
