package com.example.storyround.testing.base

import kotlin.test.assertEquals

class ActionRecorder<A> {
    private val recorded = mutableListOf<A>()

    fun record(action: A) {
        recorded += action
    }

    fun assertEmitted(vararg expected: A) = assertEquals(expected.toList(), recorded, "Reported actions")

    fun assertNothingEmitted() = assertEquals(emptyList(), recorded, "Reported actions")
}
