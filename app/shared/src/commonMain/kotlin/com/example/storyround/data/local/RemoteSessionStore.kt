package com.example.storyround.data.local

internal interface RemoteSessionStore {
    fun load(): RemoteSession?

    fun save(session: RemoteSession)

    fun clear()
}
