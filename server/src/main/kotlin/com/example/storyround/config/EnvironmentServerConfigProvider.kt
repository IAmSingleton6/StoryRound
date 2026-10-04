package com.example.storyround.config

internal class EnvironmentServerConfigProvider(
    private val environment: Map<String, String> = System.getenv(),
) : ServerConfigProvider {
    override fun load(): ServerConfig =
        ServerConfig(
            host = environment["HOST"].orDefault("0.0.0.0"),
            port = environment["PORT"].toValidPortOrDefault(8080),
            databaseUrl = environment["DATABASE_URL"]?.takeIf(String::isNotBlank),
        )

    private fun String?.orDefault(defaultValue: String): String =
        takeIf {
            !it.isNullOrBlank()
        } ?: defaultValue

    private fun String?.toValidPortOrDefault(defaultValue: Int): Int =
        this?.toIntOrNull()?.takeIf {
            it in 1..65_535
        } ?: defaultValue
}
