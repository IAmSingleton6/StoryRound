package com.example.storyround

import com.example.storyround.configuration.GameConfiguration
import io.ktor.server.testing.TestApplication
import kotlinx.coroutines.test.runTest
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertTrue

class ApplicationTest {
    @Test
    fun `given an invalid game configuration when the server starts then it refuses to start`() =
        runTest {
            // Given
            val invalidConfiguration = module { single { GameConfiguration(maxPlayers = 0) } }
            val application = TestApplication { application { storyRoundServer(listOf(invalidConfiguration)) } }

            try {
                // When
                val startup = runCatching { application.start() }

                // Then
                assertTrue(startup.isFailure)
            } finally {
                application.stop()
            }
        }
}
