package com.example.storyround.di

import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.configuration.ServerUrlProvider
import com.example.storyround.data.idea.BundledStoryIdeaProvider
import com.example.storyround.data.local.LocalStoryHistoryRepository
import com.example.storyround.data.local.RemoteSessionStore
import com.example.storyround.data.local.SettingsRemoteSessionStore
import com.example.storyround.data.local.SettingsStoryHistoryStore
import com.example.storyround.data.local.StoryHistoryStore
import com.example.storyround.data.local.storageJson
import com.example.storyround.data.remote.FixedIntervalReconnectPolicy
import com.example.storyround.data.remote.GameConnectionManager
import com.example.storyround.data.remote.GameConnectionManagerImpl
import com.example.storyround.data.remote.GameSessionReducer
import com.example.storyround.data.remote.GameSessionReducerImpl
import com.example.storyround.data.remote.GameSocket
import com.example.storyround.data.remote.KtorGameSocket
import com.example.storyround.data.remote.ReconnectPolicy
import com.example.storyround.data.remote.RemoteGameRepository
import com.example.storyround.domain.idea.StoryIdeaProvider
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.repository.StoryHistoryRepository
import com.example.storyround.logging.KermitLogger
import com.example.storyround.logging.Logger
import com.example.storyround.presentation.finished.FinishedViewModel
import com.example.storyround.presentation.history.HistoryViewModel
import com.example.storyround.presentation.history.SavedStoryViewModel
import com.example.storyround.presentation.home.HomeViewModel
import com.example.storyround.presentation.lobby.LobbyViewModel
import com.example.storyround.presentation.mapper.ContributionUiMapper
import com.example.storyround.presentation.mapper.ContributionUiMapperImpl
import com.example.storyround.presentation.mapper.GameErrorUiMapper
import com.example.storyround.presentation.mapper.GameErrorUiMapperImpl
import com.example.storyround.presentation.mapper.PlayerUiMapper
import com.example.storyround.presentation.mapper.PlayerUiMapperImpl
import com.example.storyround.presentation.mapper.RoomUiMapper
import com.example.storyround.presentation.mapper.RoomUiMapperImpl
import com.example.storyround.presentation.mapper.SavedStoryUiMapper
import com.example.storyround.presentation.mapper.SavedStoryUiMapperImpl
import com.example.storyround.presentation.mapper.StorySegmentMapper
import com.example.storyround.presentation.mapper.StorySegmentMapperImpl
import com.example.storyround.presentation.mapper.StoryUiStateMapper
import com.example.storyround.presentation.mapper.StoryUiStateMapperImpl
import com.example.storyround.presentation.navigation.AppViewModel
import com.example.storyround.presentation.story.StoryViewModel
import com.example.storyround.protocol.UserTextSanitizer
import com.example.storyround.protocol.UserTextSanitizerImpl
import com.example.storyround.protocol.UserTextValidator
import com.example.storyround.protocol.UserTextValidatorImpl
import com.example.storyround.protocol.protocolJson
import com.example.storyround.random.RandomNumberGenerator
import com.example.storyround.random.RandomNumberGeneratorImpl
import com.example.storyround.rules.TurnRules
import com.example.storyround.rules.TurnRulesImpl
import com.example.storyround.story.SavedStoryMapper
import com.example.storyround.story.SavedStoryMapperImpl
import com.example.storyround.time.SystemTimeProvider
import com.example.storyround.time.TimeProvider
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val LOG_TAG = "App"

private val BackgroundScope = named("backgroundScope")
private val ProtocolJson = named("protocolJson")
private val StorageJson = named("storageJson")

internal fun appModule(): Module =
    module {
        single { GameConfiguration() }
        single<TurnRules> { TurnRulesImpl(get()) }
        single<UserTextSanitizer> { UserTextSanitizerImpl() }
        single<UserTextValidator> { UserTextValidatorImpl(get()) }
        single<SavedStoryMapper> { SavedStoryMapperImpl() }
        single<TimeProvider> { SystemTimeProvider() }
        single<RandomNumberGenerator> { RandomNumberGeneratorImpl() }
        single<Logger> { KermitLogger(get()) }

        single(BackgroundScope) { backgroundScope(get()) }
        single { HttpClient { install(WebSockets) } }
        single(ProtocolJson) { protocolJson() }
        single(StorageJson) { storageJson() }
        single { Settings() }

        single<RemoteSessionStore> { SettingsRemoteSessionStore(get(), get(StorageJson), get()) }
        single<StoryHistoryStore> { SettingsStoryHistoryStore(get(), get(StorageJson), get()) }
        single<ReconnectPolicy> { FixedIntervalReconnectPolicy() }
        single<GameSessionReducer> { GameSessionReducerImpl() }
        single<GameSocket> { KtorGameSocket(get(), get(ProtocolJson), get()) }
        single<GameConnectionManager> { GameConnectionManagerImpl(get(), get(BackgroundScope), get()) }
        single<StoryIdeaProvider> { BundledStoryIdeaProvider(get()) }
        single<StoryHistoryRepository> { LocalStoryHistoryRepository(get(), get(), get(), get()) }
        single<GameRepository> {
            RemoteGameRepository(
                serverUrl = get<ServerUrlProvider>().serverUrl(),
                sessionStore = get(),
                storyHistoryRepository = get(),
                reconnectPolicy = get(),
                sessionReducer = get(),
                connectionManager = get(),
                scope = get(BackgroundScope),
                logger = get(),
            )
        }

        single<PlayerUiMapper> { PlayerUiMapperImpl() }
        single<GameErrorUiMapper> { GameErrorUiMapperImpl() }
        single<ContributionUiMapper> { ContributionUiMapperImpl() }
        single<StorySegmentMapper> { StorySegmentMapperImpl() }
        single<RoomUiMapper> { RoomUiMapperImpl(get(), get(), get()) }
        single<SavedStoryUiMapper> { SavedStoryUiMapperImpl(get(), get(), get()) }
        single<StoryUiStateMapper> { StoryUiStateMapperImpl(get(), get(), get()) }
        viewModelOf(::AppViewModel)
        viewModelOf(::HomeViewModel)
        viewModelOf(::LobbyViewModel)
        viewModelOf(::StoryViewModel)
        viewModelOf(::FinishedViewModel)
        viewModelOf(::HistoryViewModel)
        viewModelOf(::SavedStoryViewModel)
    }

private fun backgroundScope(logger: Logger): CoroutineScope {
    val logFailure = CoroutineExceptionHandler { _, failure -> logger.error(LOG_TAG, "Background work failed.", failure) }
    return CoroutineScope(SupervisorJob() + Dispatchers.Default + logFailure)
}
