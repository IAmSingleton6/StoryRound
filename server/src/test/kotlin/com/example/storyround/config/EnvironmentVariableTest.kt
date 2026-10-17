package com.example.storyround.config

import com.example.storyround.logging.LogLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EnvironmentVariableTest {
    @Test
    fun `given a non-blank host when parsing then the host is returned`() {
        // When
        val host = EnvironmentVariable.Host.parse("example.com")

        // Then
        assertEquals("example.com", host)
    }

    @Test
    fun `given a blank host when parsing then it is rejected`() {
        // When
        val host = EnvironmentVariable.Host.parse("   ")

        // Then
        assertNull(host)
    }

    @Test
    fun `given ports at the edges of the valid range when parsing then they are accepted`() {
        // When
        val lowest = EnvironmentVariable.Port.parse("1")
        val highest = EnvironmentVariable.Port.parse("65535")

        // Then
        assertEquals(1, lowest)
        assertEquals(65_535, highest)
    }

    @Test
    fun `given ports outside the valid range when parsing then they are rejected`() {
        // When
        val belowRange = EnvironmentVariable.Port.parse("0")
        val aboveRange = EnvironmentVariable.Port.parse("65536")

        // Then
        assertNull(belowRange)
        assertNull(aboveRange)
    }

    @Test
    fun `given a non-numeric port when parsing then it is rejected`() {
        // When
        val port = EnvironmentVariable.Port.parse("http")

        // Then
        assertNull(port)
    }

    @Test
    fun `given a log level in any case when parsing then the matching level is returned`() {
        // When
        val level = EnvironmentVariable.MinimumLogLevel.parse(" DEBUG ")

        // Then
        assertEquals(LogLevel.Debug, level)
    }

    @Test
    fun `given an unknown log level when parsing then it is rejected`() {
        // When
        val level = EnvironmentVariable.MinimumLogLevel.parse("verbose")

        // Then
        assertNull(level)
    }
}
