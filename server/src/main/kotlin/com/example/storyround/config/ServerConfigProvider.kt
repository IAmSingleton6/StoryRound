package com.example.storyround.config

internal interface ServerConfigProvider {
    fun load(): ServerConfig
}
