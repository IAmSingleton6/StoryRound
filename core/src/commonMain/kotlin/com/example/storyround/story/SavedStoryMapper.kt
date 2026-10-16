package com.example.storyround.story

import com.example.storyround.protocol.RoomState

interface SavedStoryMapper {
    fun map(
        room: RoomState,
        id: String,
        createdAtEpochMillis: Long,
    ): SavedStoryMapping
}
