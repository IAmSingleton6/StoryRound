package com.example.storyround.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test

class KermitLoggerTest {
    private val writer = mockk<LogWriter>(relaxed = true) { every { isLoggable(any(), any()) } returns true }
    private val failure = IllegalStateException("Could not send secret-token")

    @Test
    fun `given the development configuration when each severity is logged then the writer receives the message, tag and throwable`() {
        // Given
        val logger = KermitLogger(LoggingConfiguration.Development, listOf(writer))

        // When
        logger.debug("Tag", "debug message")
        logger.info("Tag", "info message")
        logger.warn("Tag", "warn message", failure)
        logger.error("Tag", "error message", failure)

        // Then
        verify {
            writer.log(Severity.Debug, "debug message", "Tag", null)
            writer.log(Severity.Info, "info message", "Tag", null)
            writer.log(Severity.Warn, "warn message", "Tag", failure)
            writer.log(Severity.Error, "error message", "Tag", failure)
        }
    }

    @Test
    fun `given the release configuration when debug detail is logged then it is not written`() {
        // Given
        val logger = KermitLogger(LoggingConfiguration.Release, listOf(writer))

        // When
        logger.debug("Tag", "debug message")
        logger.info("Tag", "info message")

        // Then
        verify(exactly = 0) { writer.log(Severity.Debug, any(), any(), any()) }
        verify { writer.log(Severity.Info, "info message", "Tag", null) }
    }

    @Test
    fun `given the release configuration when a failure is logged then only its type is written`() {
        // Given
        val logger = KermitLogger(LoggingConfiguration.Release, listOf(writer))

        // When
        logger.warn("Tag", "Could not send.", failure)

        // Then
        verify { writer.log(Severity.Warn, "Could not send. [IllegalStateException]", "Tag", null) }
    }
}
