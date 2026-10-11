package com.example.storyround.time

import kotlin.time.Clock

class SystemTimeProvider : TimeProvider {
    override fun nowEpochMillis(): Long = Clock.System.now().toEpochMilliseconds()
}
