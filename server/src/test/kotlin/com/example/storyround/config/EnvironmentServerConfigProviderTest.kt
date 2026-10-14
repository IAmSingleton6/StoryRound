package com.example.storyround.config

import kotlin.test.Test
import kotlin.test.assertEquals

class EnvironmentServerConfigProviderTest {
    @Test
    fun `given no environment overrides when loading config then defaults are used`() {
        assertEquals(
            ServerConfig(host = "0.0.0.0", port = 8080),
            EnvironmentServerConfigProvider(emptyMap()).load(),
        )
    }

    @Test
    fun `given valid environment overrides when loading config then configured values are used`() {
        assertEquals(
            ServerConfig(host = "127.0.0.1", port = 9090),
            EnvironmentServerConfigProvider(
                mapOf(
                    "HOST" to "127.0.0.1",
                    "PORT" to "9090",
                ),
            ).load(),
        )
    }

    @Test
    fun `given invalid optional environment values when loading config then safe defaults are used`() {
        val config =
            EnvironmentServerConfigProvider(
                mapOf("HOST" to "", "PORT" to "70000"),
            ).load()

        assertEquals("0.0.0.0", config.host)
        assertEquals(8080, config.port)
    }
}
