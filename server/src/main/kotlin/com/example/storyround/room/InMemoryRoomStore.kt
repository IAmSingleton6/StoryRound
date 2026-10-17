package com.example.storyround.room

internal class InMemoryRoomStore : RoomStore {
    private val rooms = mutableMapOf<String, MutableGameRoom>()

    @Synchronized
    override fun create(room: MutableGameRoom): Boolean = rooms.putIfAbsent(room.code, room) == null

    @Synchronized
    override fun remove(roomCode: String) {
        rooms.remove(roomCode)
    }

    @Synchronized
    override fun <T> withRoom(
        roomCode: String,
        block: (MutableGameRoom?) -> T,
    ): T = block(rooms[roomCode])
}
