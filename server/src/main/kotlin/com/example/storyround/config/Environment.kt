package com.example.storyround.config

internal interface Environment {
    fun <T> read(variable: EnvironmentVariable<T>): T
}
