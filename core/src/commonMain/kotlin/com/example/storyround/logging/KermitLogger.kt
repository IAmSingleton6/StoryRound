package com.example.storyround.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import co.touchlab.kermit.Logger as Kermit

class KermitLogger internal constructor(
    logWriters: List<LogWriter>,
) : Logger {
    constructor() : this(listOf(platformLogWriter()))

    private val kermit = Kermit(StaticConfig(logWriterList = logWriters))

    override fun debug(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) = kermit.log(Severity.Debug, tag, throwable, message)

    override fun info(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) = kermit.log(Severity.Info, tag, throwable, message)

    override fun warn(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) = kermit.log(Severity.Warn, tag, throwable, message)

    override fun error(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) = kermit.log(Severity.Error, tag, throwable, message)
}
