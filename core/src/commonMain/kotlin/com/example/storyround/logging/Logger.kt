package com.example.storyround.logging

expect open class Logger {
    open fun v(
        throwable: Throwable?,
        tag: String,
        message: () -> String,
    )

    open fun d(
        throwable: Throwable?,
        tag: String,
        message: () -> String,
    )

    open fun i(
        throwable: Throwable?,
        tag: String,
        message: () -> String,
    )

    open fun w(
        throwable: Throwable?,
        tag: String,
        message: () -> String,
    )

    open fun e(
        throwable: Throwable?,
        tag: String,
        message: () -> String,
    )

    open fun a(
        throwable: Throwable?,
        tag: String,
        message: () -> String,
    )

    open fun v(
        message: String,
        throwable: Throwable?,
        tag: String,
    )

    open fun d(
        message: String,
        throwable: Throwable?,
        tag: String,
    )

    open fun i(
        message: String,
        throwable: Throwable?,
        tag: String,
    )

    open fun w(
        message: String,
        throwable: Throwable?,
        tag: String,
    )

    open fun e(
        message: String,
        throwable: Throwable?,
        tag: String,
    )

    open fun a(
        message: String,
        throwable: Throwable?,
        tag: String,
    )

    open fun withTag(tag: String): Logger
}

expect fun logger(): Logger
