package com.example.storyround.config

internal class SystemEnvironment(
    private val values: Map<String, String> = System.getenv(),
) : Environment {
    override fun <T> read(variable: EnvironmentVariable<T>): T = values[variable.key]?.let(variable::parse) ?: variable.default
}
