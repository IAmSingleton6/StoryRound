package com.example.storyround.persistence

import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.SavedStory
import com.example.storyround.protocol.StorySummary

interface StoryRepository {
    fun save(
        room: RoomState,
        createdAtEpochMillis: Long,
    ): SavedStory

    fun find(id: String): SavedStory?

    fun list(): List<StorySummary>
}
