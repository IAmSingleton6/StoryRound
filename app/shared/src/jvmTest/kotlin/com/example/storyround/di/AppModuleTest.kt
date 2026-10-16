package com.example.storyround.di

import com.example.storyround.configuration.ServerUrlProvider
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.logging.LoggingConfiguration
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
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class AppModuleTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var application: KoinApplication

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        if (::application.isInitialized) application.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `should bind every dependency when each screen's view model is resolved`() {
        // Given
        val platform =
            module {
                single<ServerUrlProvider> { ServerUrlProvider { "ws://localhost:8080/game" } }
                single<Settings> { MapSettings() }
                single { LoggingConfiguration.Release }
            }
        application = koinApplication { modules(appModule(), platform) }

        // When
        val koin = application.koin
        val resolved =
            listOf(
                koin.get<GameRepository>(),
                koin.get<AppViewModel>(),
                koin.get<HomeViewModel>(),
                koin.get<LobbyViewModel>(),
                koin.get<StoryViewModel>(),
                koin.get<FinishedViewModel>(),
                koin.get<HistoryViewModel>(),
                koin.get<SavedStoryViewModel>(),
            )

        // Then
        resolved.forEach(::assertNotNull)
    }
}
