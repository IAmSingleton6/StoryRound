package com.example.storyround.data.remote

internal class FixedIntervalReconnectPolicy(
    private val intervalMillis: Long = DEFAULT_INTERVAL_MILLIS,
) : ReconnectPolicy {
    init {
        require(intervalMillis > 0) { "intervalMillis must be positive, was $intervalMillis" }
    }

    override fun shouldReconnect(
        hasSavedSession: Boolean,
        reconnectInProgress: Boolean,
    ): Boolean = hasSavedSession && !reconnectInProgress

    override fun delayMillis(attempt: Int): Long = intervalMillis

    private companion object {
        const val DEFAULT_INTERVAL_MILLIS = 1_000L
    }
}
