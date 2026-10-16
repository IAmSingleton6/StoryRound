package com.example.storyround.story

import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.SavedStory

class SavedStoryMapperImpl : SavedStoryMapper {
    override fun map(
        room: RoomState,
        id: String,
        createdAtEpochMillis: Long,
    ): SavedStoryMapping {
        if (room.status != GameStatus.Finished) return SavedStoryMapping.Incomplete("the room has not finished")
        val prompt = room.prompt ?: return SavedStoryMapping.Incomplete("the room has no opening prompt")
        return SavedStoryMapping.Mapped(
            SavedStory(
                id = id,
                prompt = prompt,
                promptPlayerId = room.openingPlayerId ?: room.hostId,
                createdAtEpochMillis = createdAtEpochMillis,
                players = room.players,
                contributions = room.contributions,
            ),
        )
    }
}
