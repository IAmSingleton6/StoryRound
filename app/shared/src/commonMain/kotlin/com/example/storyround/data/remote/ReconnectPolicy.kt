package com.example.storyround.data.remote

internal interface ReconnectPolicy {
    fun shouldReconnect(
        hasSavedSession: Boolean,
        reconnectInProgress: Boolean,
    ): Boolean

    fun delayMillis(attempt: Int): Long
}
