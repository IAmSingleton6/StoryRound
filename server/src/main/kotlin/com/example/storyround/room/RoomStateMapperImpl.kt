package com.example.storyround.room

import com.example.storyround.protocol.RoomState

internal class RoomStateMapperImpl : RoomStateMapper {
    override fun map(room: MutableGameRoom): RoomState =
        RoomState(
            roomCode = room.code,
            hostId = room.hostId,
            players = room.players.toList(),
            status = room.status,
            openingPlayerId = room.openingPlayerId,
            prompt = room.prompt,
            contributions = room.contributions.toList(),
            currentPlayerId = room.currentPlayerId,
            turnNumber = room.turnNumber,
            turnDeadlineEpochMillis = room.turnDeadlineEpochMillis,
            maxPlayers = room.maxPlayers,
            settings = room.settings,
        )
}
