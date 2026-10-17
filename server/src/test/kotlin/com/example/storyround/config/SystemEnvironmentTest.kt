package com.example.storyround.config

import kotlin.test.Test
import kotlin.test.assertEquals

class SystemEnvironmentTest {
    @Test
    fun `given no values when reading variables then defaults are returned`() {
        // When
        val environment = SystemEnvironment(emptyMap())

        // Then
        assertEquals("0.0.0.0", environment.read(EnvironmentVariable.Host))
        assertEquals(8080, environment.read(EnvironmentVariable.Port))
    }

    @Test
    fun `given valid values when reading variables then parsed values are returned`() {
        // When
        val environment =
            SystemEnvironment(
                mapOf(
                    EnvironmentVariable.Host.key to "127.0.0.1",
                    EnvironmentVariable.Port.key to "9090",
                ),
            )

        // Then
        assertEquals("127.0.0.1", environment.read(EnvironmentVariable.Host))
        assertEquals(9090, environment.read(EnvironmentVariable.Port))
    }

    @Test
    fun `given invalid values when reading variables then defaults are returned`() {
        // When
        val environment =
            SystemEnvironment(
                mapOf(
                    EnvironmentVariable.Host.key to "",
                    EnvironmentVariable.Port.key to "70000",
                ),
            )

        // Then
        assertEquals("0.0.0.0", environment.read(EnvironmentVariable.Host))
        assertEquals(8080, environment.read(EnvironmentVariable.Port))
    }
}
