package com.example.storyround.domain.repository

import com.example.storyround.domain.model.StoryHistoryState
import com.example.storyround.protocol.RoomState
import kotlinx.coroutines.flow.StateFlow

internal interface StoryHistoryRepository {
    val state: StateFlow<StoryHistoryState>

    fun load()

    fun open(id: String): Boolean

    fun close()

    fun saveFinished(room: RoomState)
}
