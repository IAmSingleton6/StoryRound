package com.example.storyround.data.remote

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FixedIntervalReconnectPolicyTest {
    @Test
    fun `given a saved session and no active retry when checked then reconnect is allowed`() {
        val policy = FixedIntervalReconnectPolicy()

        assertTrue(policy.shouldReconnect(hasSavedSession = true, reconnectInProgress = false))
    }

    @Test
    fun `given no saved session or an active retry when checked then reconnect is not allowed`() {
        val policy = FixedIntervalReconnectPolicy()

        assertFalse(policy.shouldReconnect(hasSavedSession = false, reconnectInProgress = false))
        assertFalse(policy.shouldReconnect(hasSavedSession = true, reconnectInProgress = true))
    }

    @Test
    fun `given retry attempts when delay is requested then the configured fixed interval is returned`() {
        val policy = FixedIntervalReconnectPolicy(intervalMillis = 250L)

        assertEquals(250L, policy.delayMillis(attempt = 0))
        assertEquals(250L, policy.delayMillis(attempt = 3))
    }

    @Test
    fun `given an invalid interval or attempt when used then validation fails`() {
        assertFailsWith<IllegalArgumentException> { FixedIntervalReconnectPolicy(intervalMillis = 0L) }
        assertFailsWith<IllegalArgumentException> { FixedIntervalReconnectPolicy().delayMillis(attempt = -1) }
    }
}
