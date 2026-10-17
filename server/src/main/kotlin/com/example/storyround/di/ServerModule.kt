package com.example.storyround.di

import com.example.storyround.config.Environment
import com.example.storyround.config.EnvironmentVariable
import com.example.storyround.config.SystemEnvironment
import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.game.ClientMessageHandler
import com.example.storyround.game.ClientMessageHandlerImpl
import com.example.storyround.game.GameWebSocketHandler
import com.example.storyround.game.RoomCoordinator
import com.example.storyround.game.RoomCoordinatorImpl
import com.example.storyround.game.RoomSocketHub
import com.example.storyround.game.RoomSocketHubImpl
import com.example.storyround.game.RoomTimers
import com.example.storyround.game.RoomTimersImpl
import com.example.storyround.identity.IdentifierGenerator
import com.example.storyround.identity.SecureRandomNumberGenerator
import com.example.storyround.identity.UuidIdentifierGenerator
import com.example.storyround.logging.KermitLogger
import com.example.storyround.logging.Logger
import com.example.storyround.logging.LoggingConfiguration
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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun serverModule(): Module =
    module {
        single { GameConfiguration() }
        single<Environment> { SystemEnvironment() }
        single<Logger> {
            KermitLogger(LoggingConfiguration(get<Environment>().read(EnvironmentVariable.MinimumLogLevel), includeFailureDetails = true))
        }
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
        single<RoomRegistry> { RoomRegistryImpl(get(), get(), get(), get(), get(), get(), get(), get()) }
        single<CoroutineScope> { backgroundScope(get()) }
        single<RoomSocketHub> { RoomSocketHubImpl(get(), get()) }
        single<RoomTimers> { RoomTimersImpl(get()) }
        single<RoomCoordinator> { RoomCoordinatorImpl(get(), get(), get(), get(), IDLE_ROOM_TIMEOUT_MILLIS, get()) }
        single<ClientMessageHandler> { ClientMessageHandlerImpl(get(), get(), get()) }
        single { GameWebSocketHandler(get(), get(), get(), get(), get()) }
    }

private const val LOG_TAG = "Server"

private const val IDLE_ROOM_TIMEOUT_MILLIS = 10 * 60 * 1_000L

private fun backgroundScope(logger: Logger): CoroutineScope {
    val logFailure = CoroutineExceptionHandler { _, failure -> logger.error(LOG_TAG, "Background work failed.", failure) }
    return CoroutineScope(SupervisorJob() + Dispatchers.Default + logFailure)
}
