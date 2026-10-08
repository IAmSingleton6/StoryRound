package com.example.storyround.di

import com.example.storyround.RoomSocketHub
import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.config.EnvironmentServerConfigProvider
import com.example.storyround.config.ServerConfig
import com.example.storyround.game.GameWebSocketHandler
import com.example.storyround.history.StoryHistoryRouteInstaller
import com.example.storyround.identity.IdentifierGenerator
import com.example.storyround.identity.RandomNumberGenerator
import com.example.storyround.identity.SecureRandomNumberGenerator
import com.example.storyround.identity.UuidIdentifierGenerator
import com.example.storyround.persistence.PostgresStoryRepositoryProvider
import com.example.storyround.persistence.StoryRepository
import com.example.storyround.persistence.StoryRepositoryProvider
import com.example.storyround.room.RoomRegistry
import com.example.storyround.room.RoomStateMapper
import com.example.storyround.room.RoomStateMapperImpl
import com.example.storyround.room.RoomIdentityGenerator
import com.example.storyround.room.InMemoryRoomRegistry
import com.example.storyround.room.InMemoryRoomStore
import com.example.storyround.room.RoomStore
import com.example.storyround.room.SecureRoomIdentityGenerator
import com.example.storyround.room.TurnRules
import com.example.storyround.time.SystemTimeProvider
import com.example.storyround.time.TimeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun serverModule(
    registry: RoomRegistry?,
    storyRepository: StoryRepository?,
    timeProvider: TimeProvider?,
): Module =
    module {
        single<ServerConfig> { EnvironmentServerConfigProvider().load() }
        single<TimeProvider> { timeProvider ?: SystemTimeProvider() }
        single<RoomStore> { InMemoryRoomStore() }
        single { GameConfiguration() }
        single<RandomNumberGenerator> { SecureRandomNumberGenerator() }
        single<IdentifierGenerator> { UuidIdentifierGenerator() }
        single<RoomIdentityGenerator> { SecureRoomIdentityGenerator(get(), get()) }
        single { TurnRules(get()) }
        single<RoomStateMapper> { RoomStateMapperImpl() }
        single<RoomRegistry> { registry ?: InMemoryRoomRegistry(get(), get(), get(), get(), get(), get(), get()) }
        single<StoryRepositoryProvider> { PostgresStoryRepositoryProvider(get()) }
        single {
            StoryRepositoryAvailability(
                storyRepository ?: get<StoryRepositoryProvider>().provide(get<ServerConfig>().databaseUrl),
            )
        }
        single { Json { classDiscriminator = "type" } }
        single { RoomSocketHub(get()) }
        single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
        single { StoryHistoryRouteInstaller(get(), get()) }
        single {
            GameWebSocketHandler(
                registry = get(),
                sockets = get(),
                storyRepository = get<StoryRepositoryAvailability>().repository,
                json = get(),
                relayScope = get(),
                timeProvider = get(),
            )
        }
    }
