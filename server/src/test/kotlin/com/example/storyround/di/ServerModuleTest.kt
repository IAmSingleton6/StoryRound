package com.example.storyround.di

import com.example.storyround.game.GameWebSocketHandler
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertNotNull

class ServerModuleTest {
    @Test
    fun `given the server module when the route entry point is resolved then every dependency is bound`() {
        val koin = koinApplication { modules(serverModule()) }.koin

        assertNotNull(koin.get<GameWebSocketHandler>())
    }
}
