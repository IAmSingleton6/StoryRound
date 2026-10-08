package com.example.storyround.logging

import co.touchlab.kermit.LoggerConfig
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import co.touchlab.kermit.Logger as KermitLogger

actual open class Logger internal constructor(
    private val delegate: KermitLogger,
) {
    actual open fun v(
        throwable: Throwable?,
        tag: String,
        message: () -> String,
    ) {
        delegate.v(throwable, tag, message)
    }

    actual open fun d(
        throwable: Throwable?,
        tag: String,
        message: () -> String,
    ) {
        delegate.d(throwable, tag, message)
    }

    actual open fun i(
        throwable: Throwable?,
        tag: String,
        message: () -> String,
    ) {
        delegate.i(throwable, tag, message)
    }

    actual open fun w(
        throwable: Throwable?,
        tag: String,
        message: () -> String,
    ) {
        delegate.w(throwable, tag, message)
    }

    actual open fun e(
        throwable: Throwable?,
        tag: String,
        message: () -> String,
    ) {
        delegate.e(throwable, tag, message)
    }

    actual open fun a(
        throwable: Throwable?,
        tag: String,
        message: () -> String,
    ) {
        delegate.a(throwable, tag, message)
    }

    actual open fun v(
        message: String,
        throwable: Throwable?,
        tag: String,
    ) {
        delegate.v(message, throwable, tag)
    }

    actual open fun d(
        message: String,
        throwable: Throwable?,
        tag: String,
    ) {
        delegate.d(message, throwable, tag)
    }

    actual open fun i(
        message: String,
        throwable: Throwable?,
        tag: String,
    ) {
        delegate.i(message, throwable, tag)
    }

    actual open fun w(
        message: String,
        throwable: Throwable?,
        tag: String,
    ) {
        delegate.w(message, throwable, tag)
    }

    actual open fun e(
        message: String,
        throwable: Throwable?,
        tag: String,
    ) {
        delegate.e(message, throwable, tag)
    }

    actual open fun a(
        message: String,
        throwable: Throwable?,
        tag: String,
    ) {
        delegate.a(message, throwable, tag)
    }

    actual open fun withTag(tag: String): Logger = Logger(delegate.withTag(tag))
}

actual fun logger(): Logger {
    val config: LoggerConfig = StaticConfig(logWriterList = listOf(platformLogWriter()))
    return Logger(KermitLogger(config))
}
