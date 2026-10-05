package com.example.storyround.data.time

import com.example.storyround.domain.time.TimeProvider
import kotlin.time.Clock

internal class SystemTimeProvider : TimeProvider {
    override fun nowEpochMillis(): Long = Clock.System.now().toEpochMilliseconds()
}
