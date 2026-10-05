package com.example.storyround.domain.time

internal interface TimeProvider {
    fun nowEpochMillis(): Long
}
