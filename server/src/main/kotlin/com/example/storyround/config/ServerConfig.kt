package com.example.storyround.config

internal data class ServerConfig(
    val host: String,
    val port: Int,
    val databaseUrl: String?,
)
