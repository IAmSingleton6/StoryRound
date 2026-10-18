package com.example.storyround.testing.actor

import com.example.storyround.time.TimeProvider
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Duration

class DeviceClock {
    @Volatile
    var nowEpochMillis: Long = START_EPOCH_MILLIS
        private set

    val timeProvider: TimeProvider = mockk { every { nowEpochMillis() } answers { nowEpochMillis } }

    fun advanceBy(duration: Duration) {
        nowEpochMillis += duration.inWholeMilliseconds
    }

    private companion object {
        const val START_EPOCH_MILLIS = 1_767_225_600_000L
    }
}
