package com.example.storyround.di

import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.configuration.ServerUrlProvider
import com.example.storyround.data.idea.BundledStoryIdeaProvider
import com.example.storyround.data.local.RemoteSessionStore
import com.example.storyround.data.local.SettingsRemoteSessionStore
import com.example.storyround.data.local.SettingsStoryHistoryStore
import com.example.storyround.data.local.StoryHistoryStore
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
import com.example.storyround.domain.usecase.CloseSavedStoryUseCase
import com.example.storyround.domain.usecase.CloseSavedStoryUseCaseImpl
import com.example.storyround.domain.usecase.ConnectToGameUseCase
import com.example.storyround.domain.usecase.ConnectToGameUseCaseImpl
import com.example.storyround.domain.usecase.CreateRoomUseCase
import com.example.storyround.domain.usecase.CreateRoomUseCaseImpl
import com.example.storyround.domain.usecase.DisconnectFromGameUseCase
import com.example.storyround.domain.usecase.DisconnectFromGameUseCaseImpl
import com.example.storyround.domain.usecase.JoinRoomUseCase
import com.example.storyround.domain.usecase.JoinRoomUseCaseImpl
import com.example.storyround.domain.usecase.LeaveRoomUseCase
import com.example.storyround.domain.usecase.LeaveRoomUseCaseImpl
import com.example.storyround.domain.usecase.LoadStoryHistoryUseCase
import com.example.storyround.domain.usecase.LoadStoryHistoryUseCaseImpl
import com.example.storyround.domain.usecase.OpenSavedStoryUseCase
import com.example.storyround.domain.usecase.OpenSavedStoryUseCaseImpl
import com.example.storyround.domain.usecase.PlayAgainUseCase
import com.example.storyround.domain.usecase.PlayAgainUseCaseImpl
import com.example.storyround.domain.usecase.StartGameUseCase
import com.example.storyround.domain.usecase.StartGameUseCaseImpl
import com.example.storyround.domain.usecase.SubmitContributionUseCase
import com.example.storyround.domain.usecase.SubmitContributionUseCaseImpl
import com.example.storyround.domain.usecase.SubmitPromptUseCase
import com.example.storyround.domain.usecase.SubmitPromptUseCaseImpl
import com.example.storyround.logging.KermitLogger
import com.example.storyround.logging.Logger
import com.example.storyround.presentation.finished.FinishedViewModel
import com.example.storyround.presentation.history.HistoryViewModel
import com.example.storyround.presentation.history.SavedStoryViewModel
import com.example.storyround.presentation.home.HomeViewModel
import com.example.storyround.presentation.lobby.LobbyViewModel
import com.example.storyround.presentation.mapper.ContributionUiMapper
import com.example.storyround.presentation.mapper.ContributionUiMapperImpl
import com.example.storyround.presentation.mapper.PlayerUiMapper
import com.example.storyround.presentation.mapper.PlayerUiMapperImpl
import com.example.storyround.presentation.mapper.RoomUiMapper
import com.example.storyround.presentation.mapper.RoomUiMapperImpl
import com.example.storyround.presentation.mapper.SavedStoryUiMapper
import com.example.storyround.presentation.mapper.SavedStoryUiMapperImpl
import com.example.storyround.presentation.mapper.StorySegmentMapper
import com.example.storyround.presentation.mapper.StorySegmentMapperImpl
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val appModule =
    module {
        single { GameConfiguration() }
        single<TurnRules> { TurnRulesImpl(get()) }
        single<UserTextSanitizer> { UserTextSanitizerImpl() }
        single<UserTextValidator> { UserTextValidatorImpl(get()) }
        single<SavedStoryMapper> { SavedStoryMapperImpl() }
        single<TimeProvider> { SystemTimeProvider() }
        single<RandomNumberGenerator> { RandomNumberGeneratorImpl() }
        single<Logger> { KermitLogger() }

        single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
        single { HttpClient { install(WebSockets) } }
        single { protocolJson() }
        single { Settings() }

        single<RemoteSessionStore> { SettingsRemoteSessionStore(get()) }
        single<StoryHistoryStore> { SettingsStoryHistoryStore(get()) }
        single<ReconnectPolicy> { FixedIntervalReconnectPolicy() }
        single<GameSessionReducer> { GameSessionReducerImpl() }
        single<GameSocket> { KtorGameSocket(get(), get()) }
        single<GameConnectionManager> { GameConnectionManagerImpl(get(), get()) }
        single<StoryIdeaProvider> { BundledStoryIdeaProvider(get()) }
        single<GameRepository> {
            RemoteGameRepository(
                serverUrl = get<ServerUrlProvider>().serverUrl(),
                sessionStore = get(),
                storyHistoryStore = get(),
                reconnectPolicy = get(),
                sessionReducer = get(),
                connectionManager = get(),
                savedStoryMapper = get(),
                timeProvider = get(),
                scope = get(),
                logger = get(),
            )
        }

        factory<ConnectToGameUseCase> { ConnectToGameUseCaseImpl(get()) }
        factory<CreateRoomUseCase> { CreateRoomUseCaseImpl(get()) }
        factory<JoinRoomUseCase> { JoinRoomUseCaseImpl(get()) }
        factory<LeaveRoomUseCase> { LeaveRoomUseCaseImpl(get()) }
        factory<DisconnectFromGameUseCase> { DisconnectFromGameUseCaseImpl(get()) }
        factory<StartGameUseCase> { StartGameUseCaseImpl(get()) }
        factory<SubmitPromptUseCase> { SubmitPromptUseCaseImpl(get()) }
        factory<SubmitContributionUseCase> { SubmitContributionUseCaseImpl(get()) }
        factory<PlayAgainUseCase> { PlayAgainUseCaseImpl(get()) }
        factory<LoadStoryHistoryUseCase> { LoadStoryHistoryUseCaseImpl(get()) }
        factory<OpenSavedStoryUseCase> { OpenSavedStoryUseCaseImpl(get()) }
        factory<CloseSavedStoryUseCase> { CloseSavedStoryUseCaseImpl(get()) }

        single<PlayerUiMapper> { PlayerUiMapperImpl() }
        single<ContributionUiMapper> { ContributionUiMapperImpl() }
        single<StorySegmentMapper> { StorySegmentMapperImpl() }
        single<RoomUiMapper> { RoomUiMapperImpl(get(), get(), get()) }
        single<SavedStoryUiMapper> { SavedStoryUiMapperImpl(get(), get(), get()) }
        viewModelOf(::AppViewModel)
        viewModelOf(::HomeViewModel)
        viewModelOf(::LobbyViewModel)
        viewModelOf(::StoryViewModel)
        viewModelOf(::FinishedViewModel)
        viewModelOf(::HistoryViewModel)
        viewModelOf(::SavedStoryViewModel)
    }
