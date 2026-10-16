package com.example.storyround.logging

internal class RecordingLogger : Logger {
    val warnings = mutableListOf<String>()

    override fun debug(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) = Unit

    override fun info(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) = Unit

    override fun warn(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        warnings += message
    }

    override fun error(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) = Unit
}
