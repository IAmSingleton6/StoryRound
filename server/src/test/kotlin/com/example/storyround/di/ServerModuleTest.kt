package com.example.storyround.di

import com.example.storyround.game.GameWebSocketHandler
import com.example.storyround.history.StoryHistoryRouteInstaller
import com.example.storyround.persistence.StoryRepository
import io.mockk.mockk
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertNotNull

class ServerModuleTest {
    @Test
    fun `given the server module when the route entry points are resolved then every dependency is bound`() {
        val koin = koinApplication { modules(serverModule(storyRepository = mockk<StoryRepository>())) }.koin

        assertNotNull(koin.get<GameWebSocketHandler>())
        assertNotNull(koin.get<StoryHistoryRouteInstaller>())
    }
}
