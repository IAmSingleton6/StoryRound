package com.example.storyround.room

internal interface RoomStore {
    fun create(room: MutableGameRoom): Boolean

    fun remove(roomCode: String)

    fun <T> withRoom(
        roomCode: String,
        block: (MutableGameRoom?) -> T,
    ): T
}
