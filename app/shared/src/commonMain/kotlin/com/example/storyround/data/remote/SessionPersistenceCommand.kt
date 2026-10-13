package com.example.storyround.data.remote

import com.example.storyround.data.local.RemoteSession

internal sealed interface SessionPersistenceCommand {
    data class Save(
        val session: RemoteSession,
    ) : SessionPersistenceCommand

    data object Clear : SessionPersistenceCommand
}
