package com.example.storyround.di

import com.example.storyround.configuration.ServerUrlProvider
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.presentation.finished.FinishedViewModel
import com.example.storyround.presentation.history.HistoryViewModel
import com.example.storyround.presentation.history.SavedStoryViewModel
import com.example.storyround.presentation.home.HomeViewModel
import com.example.storyround.presentation.lobby.LobbyViewModel
import com.example.storyround.presentation.navigation.AppViewModel
import com.example.storyround.presentation.story.StoryViewModel
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class AppModuleTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given the app module and a server url when every screen is resolved then all dependencies are bound`() {
        val platform =
            module {
                single<ServerUrlProvider> { ServerUrlProvider { "ws://localhost:8080/game" } }
                single<Settings> { MapSettings() }
            }
        val koin = koinApplication { modules(appModule, platform) }.koin

        assertNotNull(koin.get<GameRepository>())
        assertNotNull(koin.get<AppViewModel>())
        assertNotNull(koin.get<HomeViewModel>())
        assertNotNull(koin.get<LobbyViewModel>())
        assertNotNull(koin.get<StoryViewModel>())
        assertNotNull(koin.get<FinishedViewModel>())
        assertNotNull(koin.get<HistoryViewModel>())
        assertNotNull(koin.get<SavedStoryViewModel>())
    }
}
