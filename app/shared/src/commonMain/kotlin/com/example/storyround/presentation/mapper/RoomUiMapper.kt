package com.example.storyround.presentation.mapper

import com.example.storyround.presentation.model.RoomUi
import com.example.storyround.protocol.RoomState

internal interface RoomUiMapper {
    fun map(room: RoomState): RoomUi
}
