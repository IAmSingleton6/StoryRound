package com.example.storyround.config

import com.example.storyround.logging.LogLevel

internal sealed class EnvironmentVariable<T>(
    val key: String,
    val default: T,
) {
    abstract fun parse(raw: String): T?

    data object Host : EnvironmentVariable<String>(key = "HOST", default = "0.0.0.0") {
        override fun parse(raw: String): String? = raw.takeIf { it.isNotBlank() }
    }

    data object Port : EnvironmentVariable<Int>(key = "PORT", default = 8080) {
        private val validPorts = 1..65_535

        override fun parse(raw: String): Int? = raw.toIntOrNull()?.takeIf { it in validPorts }
    }

    data object MinimumLogLevel : EnvironmentVariable<LogLevel>(key = "LOG_LEVEL", default = LogLevel.Info) {
        override fun parse(raw: String): LogLevel? = LogLevel.entries.firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) }
    }
}
