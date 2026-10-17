package com.example.storyround.config

internal class ServerConfigProviderImpl(
    private val environment: Environment,
) : ServerConfigProvider {
    override fun load(): ServerConfig =
        ServerConfig(
            host = environment.read(EnvironmentVariable.Host),
            port = environment.read(EnvironmentVariable.Port),
        )
}
