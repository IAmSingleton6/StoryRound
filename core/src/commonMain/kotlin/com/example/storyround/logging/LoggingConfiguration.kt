package com.example.storyround.logging

data class LoggingConfiguration(
    val minimumLevel: LogLevel,
    val includeFailureDetails: Boolean,
) {
    companion object {
        val Development = LoggingConfiguration(LogLevel.Debug, includeFailureDetails = true)
        val Release = LoggingConfiguration(LogLevel.Info, includeFailureDetails = false)
    }
}
