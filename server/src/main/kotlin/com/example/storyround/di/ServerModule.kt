package com.example.storyround.di

import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.game.ClientMessageDispatcher
import com.example.storyround.game.ClientMessageDispatcherImpl
import com.example.storyround.game.GameWebSocketHandler
import com.example.storyround.game.RoomSocketHub
import com.example.storyround.game.RoomSocketHubImpl
import com.example.storyround.game.TurnExpiryScheduler
import com.example.storyround.game.TurnExpirySchedulerImpl
import com.example.storyround.identity.IdentifierGenerator
import com.example.storyround.identity.SecureRandomNumberGenerator
import com.example.storyround.identity.UuidIdentifierGenerator
import com.example.storyround.protocol.UserTextSanitizer
import com.example.storyround.protocol.UserTextSanitizerImpl
import com.example.storyround.protocol.UserTextValidator
import com.example.storyround.protocol.UserTextValidatorImpl
import com.example.storyround.protocol.protocolJson
import com.example.storyround.random.RandomNumberGenerator
import com.example.storyround.room.InMemoryRoomStore
import com.example.storyround.room.RoomIdentityGenerator
import com.example.storyround.room.RoomRegistry
import com.example.storyround.room.RoomRegistryImpl
import com.example.storyround.room.RoomStateMapper
import com.example.storyround.room.RoomStateMapperImpl
import com.example.storyround.room.RoomStore
import com.example.storyround.room.SecureRoomIdentityGenerator
import com.example.storyround.rules.TurnRules
import com.example.storyround.rules.TurnRulesImpl
import com.example.storyround.time.SystemTimeProvider
import com.example.storyround.time.TimeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun serverModule(registry: RoomRegistry? = null): Module =
    module {
        single { GameConfiguration() }
        single { protocolJson() }
        single<TimeProvider> { SystemTimeProvider() }
        single<RandomNumberGenerator> { SecureRandomNumberGenerator() }
        single<IdentifierGenerator> { UuidIdentifierGenerator() }
        single<UserTextSanitizer> { UserTextSanitizerImpl() }
        single<UserTextValidator> { UserTextValidatorImpl(get()) }
        single<TurnRules> { TurnRulesImpl(get()) }
        single<RoomStore> { InMemoryRoomStore() }
        single<RoomIdentityGenerator> { SecureRoomIdentityGenerator(get(), get()) }
        single<RoomStateMapper> { RoomStateMapperImpl() }
        single<RoomRegistry> { registry ?: RoomRegistryImpl(get(), get(), get(), get(), get(), get(), get(), get()) }
        single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
        single<RoomSocketHub> { RoomSocketHubImpl(get()) }
        single<ClientMessageDispatcher> { ClientMessageDispatcherImpl(get()) }
        single<TurnExpiryScheduler> { TurnExpirySchedulerImpl(get(), get(), get()) }
        single { GameWebSocketHandler(get(), get(), get(), get()) }
    }
