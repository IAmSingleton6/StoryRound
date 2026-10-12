package com.example.storyround.logging

interface Logger {
    fun debug(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    )

    fun info(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    )

    fun warn(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    )

    fun error(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    )
}
