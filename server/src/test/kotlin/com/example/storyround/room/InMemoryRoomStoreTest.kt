package com.example.storyround.room

import com.example.storyround.protocol.RoomSettings
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InMemoryRoomStoreTest {
    private val store = InMemoryRoomStore()
    private val room =
        MutableGameRoom(code = "AB12", hostId = "host", players = mutableListOf(), maxPlayers = 10, settings = RoomSettings(rounds = 3))

    @Test
    fun `given a room code already in use when another room is created with it then creation is refused`() {
        // When
        val firstCreated = store.create(room)
        val secondCreated =
            store.create(
                MutableGameRoom("AB12", "other", mutableListOf(), maxPlayers = 10, settings = RoomSettings(rounds = 3)),
            )

        // Then
        assertTrue(firstCreated)
        assertFalse(secondCreated)
    }

    @Test
    fun `given a removed room when looked up then it is gone`() {
        // Given
        store.create(room)

        // When
        store.remove("AB12")

        // Then
        assertNull(store.withRoom("AB12") { it })
    }
}
