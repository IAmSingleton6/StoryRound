package com.example.storyround.game

import com.example.storyround.room.RegistryResult

internal interface CompletedStoryService {
    suspend fun saveIfFinished(result: RegistryResult.Success): StorySaveResult

    fun savedStoryId(roomCode: String): String?

    fun forget(roomCode: String)
}
