package com.example.storyround.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test

class KermitLoggerTest {
    private val writer = mockk<LogWriter>(relaxed = true) { every { isLoggable(any(), any()) } returns true }
    private val logger = KermitLogger(listOf(writer))

    @Test
    fun `given each severity when logged then the writer receives the message, tag and throwable`() {
        val failure = IllegalStateException("offline")

        logger.debug("Tag", "debug message")
        logger.info("Tag", "info message")
        logger.warn("Tag", "warn message", failure)
        logger.error("Tag", "error message", failure)

        verify {
            writer.log(Severity.Debug, "debug message", "Tag", null)
            writer.log(Severity.Info, "info message", "Tag", null)
            writer.log(Severity.Warn, "warn message", "Tag", failure)
            writer.log(Severity.Error, "error message", "Tag", failure)
        }
    }
}
