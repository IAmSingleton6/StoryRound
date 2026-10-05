package com.example.storyround.data.local

import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json

internal class SettingsRemoteSessionStore(
    private val settings: Settings,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : RemoteSessionStore {
    override fun load(): RemoteSession? =
        settings
            .getStringOrNull(REMOTE_SESSION_KEY)
            ?.let { serialized -> runCatching { json.decodeFromString<RemoteSession>(serialized) }.getOrNull() }

    override fun save(session: RemoteSession) {
        settings.putString(REMOTE_SESSION_KEY, json.encodeToString(session))
    }

    override fun clear() {
        settings.remove(REMOTE_SESSION_KEY)
    }

    private companion object {
        const val REMOTE_SESSION_KEY = "story_round.remote_session"
    }
}
