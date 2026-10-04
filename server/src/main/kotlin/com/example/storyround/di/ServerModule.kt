package com.example.storyround.di

import com.example.storyround.RoomSocketHub
import com.example.storyround.config.EnvironmentServerConfigProvider
import com.example.storyround.config.ServerConfig
import com.example.storyround.game.GameWebSocketHandler
import com.example.storyround.history.StoryHistoryRouteInstaller
import com.example.storyround.persistence.PostgresStoryRepositoryProvider
import com.example.storyround.persistence.StoryRepository
import com.example.storyround.persistence.StoryRepositoryProvider
import com.example.storyround.room.RoomRegistry
import com.example.storyround.time.TimeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal fun serverModule(
    registry: RoomRegistry,
    storyRepository: StoryRepository?,
    timeProvider: TimeProvider,
): Module =
    module {
        single<ServerConfig> { EnvironmentServerConfigProvider().load() }
        single<RoomRegistry> { registry }
        single<TimeProvider> { timeProvider }
        single<StoryRepositoryProvider> { PostgresStoryRepositoryProvider() }
        single {
            StoryRepositoryAvailability(
                storyRepository ?: get<StoryRepositoryProvider>().provide(get<ServerConfig>().databaseUrl),
            )
        }
        single { Json { classDiscriminator = "type" } }
        single { RoomSocketHub(get()) }
        single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
        single { StoryHistoryRouteInstaller(get(), get()) }
        single<Logger> { LoggerFactory.getLogger(GameWebSocketHandler::class.java) }
        single {
            GameWebSocketHandler(
                registry = get(),
                sockets = get(),
                storyRepository = get<StoryRepositoryAvailability>().repository,
                json = get(),
                relayScope = get(),
                logger = get(),
                timeProvider = get(),
            )
        }
    }
