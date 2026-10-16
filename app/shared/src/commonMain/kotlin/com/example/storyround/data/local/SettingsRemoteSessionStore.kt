package com.example.storyround.data.local

import com.example.storyround.logging.Logger
import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json

private const val LOG_TAG = "SettingsRemoteSessionStore"

internal class SettingsRemoteSessionStore(
    private val settings: Settings,
    private val json: Json,
    private val logger: Logger,
) : RemoteSessionStore {
    override fun load(): RemoteSession? =
        settings
            .getStringOrNull(REMOTE_SESSION_KEY)
            ?.let { serialized ->
                runCatching { json.decodeFromString<RemoteSession>(serialized) }
                    .onFailure { logger.warn(LOG_TAG, "Ignored an unreadable saved session (${it::class.simpleName}).") }
                    .getOrNull()
            }

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
