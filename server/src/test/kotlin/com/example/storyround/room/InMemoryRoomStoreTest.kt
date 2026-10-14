package com.example.storyround.room

import com.example.storyround.protocol.RoomSettings
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class InMemoryRoomStoreTest {
    private val store = InMemoryRoomStore()
    private val room =
        MutableGameRoom(code = "AB12", hostId = "host", players = mutableListOf(), maxPlayers = 10, settings = RoomSettings(rounds = 3))

    @Test
    fun `given a room code already in use when another room is created with it then creation is refused`() {
        assertTrue(store.create(room))
        assertFalse(store.create(MutableGameRoom("AB12", "other", mutableListOf(), maxPlayers = 10, settings = RoomSettings(rounds = 3))))
    }

    @Test
    fun `given a stored room when looked up with a lower-case padded code then the same room is found`() {
        store.create(room)

        assertSame(room, store.withRoom(" ab12 ") { it })
    }

    @Test
    fun `given a removed room when looked up then it is gone`() {
        store.create(room)

        store.remove("ab12")

        assertNull(store.withRoom("AB12") { it })
    }
}
