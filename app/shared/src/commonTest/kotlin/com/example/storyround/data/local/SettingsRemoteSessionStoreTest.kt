package com.example.storyround.data.local

import com.example.storyround.logging.RecordingLogger
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SettingsRemoteSessionStoreTest {
    private val logger = RecordingLogger()

    @Test
    fun `given a saved session when loaded then the original values are restored`() {
        // Given
        val store = SettingsRemoteSessionStore(MapSettings(), storageJson(), logger)
        val session = RemoteSession(roomCode = "7F2K", playerId = "player-1", sessionToken = "token-1")

        // When
        store.save(session)

        // Then
        assertEquals(session, store.load())
    }

    @Test
    fun `given a saved session when cleared then loading returns no session`() {
        // Given
        val store = SettingsRemoteSessionStore(MapSettings(), storageJson(), logger)
        store.save(RemoteSession(roomCode = "7F2K", playerId = "player-1", sessionToken = "token-1"))

        // When
        store.clear()

        // Then
        assertNull(store.load())
    }

    @Test
    fun `given an unreadable saved session when loaded then there is no session and a warning without its contents is logged`() {
        // Given
        val settings = MapSettings()
        settings.putString("story_round.remote_session", "{\"sessionToken\":\"secret-token\"")
        val store = SettingsRemoteSessionStore(settings, storageJson(), logger)

        // When
        val session = store.load()

        // Then
        assertNull(session)
        assertEquals(1, logger.warnings.size)
        assertTrue("secret-token" !in logger.warnings.single())
    }
}
