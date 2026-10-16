package com.example.storyround.data.remote

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class FixedIntervalReconnectPolicyTest {
    @Test
    fun `given a saved session and no active retry when checked then reconnect is allowed`() {
        // Given
        val policy = FixedIntervalReconnectPolicy()

        // When
        val allowed = policy.shouldReconnect(hasSavedSession = true, reconnectInProgress = false)

        // Then
        assertTrue(allowed)
    }

    @Test
    fun `given no saved session or an active retry when checked then reconnect is not allowed`() {
        // Given
        val policy = FixedIntervalReconnectPolicy()

        // When
        val withoutSession = policy.shouldReconnect(hasSavedSession = false, reconnectInProgress = false)
        val duringRetry = policy.shouldReconnect(hasSavedSession = true, reconnectInProgress = true)

        // Then
        assertFalse(withoutSession)
        assertFalse(duringRetry)
    }

    @Test
    fun `given retry attempts when delay is requested then the configured fixed interval is returned`() {
        // Given
        val policy = FixedIntervalReconnectPolicy(intervalMillis = 250L)

        // When
        val firstDelay = policy.delayMillis(attempt = 0)
        val laterDelay = policy.delayMillis(attempt = 3)

        // Then
        assertEquals(250L, firstDelay)
        assertEquals(250L, laterDelay)
    }

    @Test
    fun `given a non-positive interval when created then validation fails with the interval`() {
        // When
        val invalidInterval = runCatching { FixedIntervalReconnectPolicy(intervalMillis = 0L) }

        // Then
        val failure = assertIs<IllegalArgumentException>(invalidInterval.exceptionOrNull())
        assertEquals("intervalMillis must be positive, was 0", failure.message)
    }
}
