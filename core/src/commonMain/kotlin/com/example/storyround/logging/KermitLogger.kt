package com.example.storyround.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import co.touchlab.kermit.Logger as Kermit

class KermitLogger internal constructor(
    private val configuration: LoggingConfiguration,
    logWriters: List<LogWriter>,
) : Logger {
    constructor(configuration: LoggingConfiguration) : this(configuration, listOf(platformLogWriter()))

    private val kermit = Kermit(StaticConfig(minSeverity = configuration.minimumLevel.toSeverity(), logWriterList = logWriters))

    override fun debug(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) = log(Severity.Debug, tag, message, throwable)

    override fun info(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) = log(Severity.Info, tag, message, throwable)

    override fun warn(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) = log(Severity.Warn, tag, message, throwable)

    override fun error(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) = log(Severity.Error, tag, message, throwable)

    private fun log(
        severity: Severity,
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        if (throwable == null || configuration.includeFailureDetails) {
            kermit.log(severity, tag, throwable, message)
        } else {
            kermit.log(severity, tag, null, "$message [${throwable::class.simpleName}]")
        }
    }

    private fun LogLevel.toSeverity(): Severity =
        when (this) {
            LogLevel.Debug -> Severity.Debug
            LogLevel.Info -> Severity.Info
            LogLevel.Warn -> Severity.Warn
            LogLevel.Error -> Severity.Error
        }
}
