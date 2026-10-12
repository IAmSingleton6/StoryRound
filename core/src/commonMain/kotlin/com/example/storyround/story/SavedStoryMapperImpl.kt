package com.example.storyround.story

import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.SavedStory

class SavedStoryMapperImpl : SavedStoryMapper {
    override fun map(
        room: RoomState,
        id: String,
        createdAtEpochMillis: Long,
    ): SavedStory {
        require(room.status == GameStatus.Finished) { "Only a finished room has a complete story." }
        return SavedStory(
            id = id,
            prompt = requireNotNull(room.prompt) { "A finished room always has an opening prompt." },
            promptPlayerId = room.openingPlayerId ?: room.hostId,
            createdAtEpochMillis = createdAtEpochMillis,
            players = room.players,
            contributions = room.contributions,
        )
    }
}
