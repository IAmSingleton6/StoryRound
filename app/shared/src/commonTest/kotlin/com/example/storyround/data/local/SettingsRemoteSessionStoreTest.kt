package com.example.storyround.data.local

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SettingsRemoteSessionStoreTest {
    @Test
    fun `given a saved session when loaded then the original values are restored`() {
        val store = SettingsRemoteSessionStore(MapSettings())
        val session = RemoteSession(roomCode = "7F2K", playerId = "player-1", sessionToken = "token-1")

        store.save(session)

        assertEquals(session, store.load())
    }

    @Test
    fun `given a saved session when cleared then loading returns no session`() {
        val store = SettingsRemoteSessionStore(MapSettings())
        store.save(RemoteSession(roomCode = "7F2K", playerId = "player-1", sessionToken = "token-1"))

        store.clear()

        assertNull(store.load())
    }
}
