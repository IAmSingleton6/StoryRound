package com.example.storyround.story

import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.SavedStory

interface SavedStoryMapper {
    fun map(
        room: RoomState,
        id: String,
        createdAtEpochMillis: Long,
    ): SavedStory
}
