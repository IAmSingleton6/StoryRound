package com.example.storyround.config

import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class ServerConfigProviderImplTest {
    private val environment = mockk<Environment>()

    @Test
    fun `given environment values when loading config then config is built from them`() {
        // Given
        every { environment.read(EnvironmentVariable.Host) } returns "127.0.0.1"
        every { environment.read(EnvironmentVariable.Port) } returns 9090

        // When
        val config = ServerConfigProviderImpl(environment).load()

        // Then
        assertEquals(ServerConfig(host = "127.0.0.1", port = 9090), config)
    }
}
