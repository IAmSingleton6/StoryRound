package com.example.storyround.room

import com.example.storyround.protocol.RoomState

internal interface RoomStateMapper {
    fun map(room: MutableGameRoom): RoomState
}
