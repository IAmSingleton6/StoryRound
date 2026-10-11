package com.example.storyround.di

import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.configuration.ServerUrlProvider
import com.example.storyround.data.idea.BundledStoryIdeaProvider
import com.example.storyround.data.local.RemoteSessionStore
import com.example.storyround.data.local.SettingsRemoteSessionStore
import com.example.storyround.data.local.SettingsStoryHistoryStore
import com.example.storyround.data.local.StoryHistoryStore
import com.example.storyround.data.remote.DefaultGameConnectionManager
import com.example.storyround.data.remote.FixedIntervalReconnectPolicy
import com.example.storyround.data.remote.GameConnectionManager
import com.example.storyround.data.remote.GameSessionReducer
import com.example.storyround.data.remote.GameSocket
import com.example.storyround.data.remote.KtorGameSocket
import com.example.storyround.data.remote.ReconnectPolicy
import com.example.storyround.data.remote.RemoteGameRepository
import com.example.storyround.domain.idea.StoryIdeaProvider
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.usecase.ConnectToGameUseCase
import com.example.storyround.domain.usecase.ConnectToGameUseCaseImpl
import com.example.storyround.domain.usecase.CreateRoomUseCase
import com.example.storyround.domain.usecase.CreateRoomUseCaseImpl
import com.example.storyround.domain.usecase.JoinRoomUseCase
import com.example.storyround.domain.usecase.JoinRoomUseCaseImpl
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
import com.example.storyround.presentation.navigation.AppViewModel
import com.example.storyround.presentation.story.StoryViewModel
import com.example.storyround.protocol.DefaultUserTextSanitizer
import com.example.storyround.protocol.UserTextSanitizer
import com.example.storyround.random.DefaultRandomNumberGenerator
import com.example.storyround.random.RandomNumberGenerator
import com.example.storyround.time.SystemTimeProvider
import com.example.storyround.time.TimeProvider
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import org.koin.dsl.module

internal val appModule =
    module {
        single { HttpClient { install(WebSockets) } }
        single { GameConfiguration() }
        single<RemoteSessionStore> { SettingsRemoteSessionStore(Settings()) }
        single<ReconnectPolicy> { FixedIntervalReconnectPolicy() }
        single<TimeProvider> { SystemTimeProvider() }
        single<RandomNumberGenerator> { DefaultRandomNumberGenerator() }
        single<UserTextSanitizer> { DefaultUserTextSanitizer() }
        single<StoryIdeaProvider> { BundledStoryIdeaProvider(get()) }
        single { GameSessionReducer() }
        single<GameSocket> { KtorGameSocket(get()) }
        single<GameConnectionManager> { DefaultGameConnectionManager(get()) }
        single<PlayerUiMapper> { PlayerUiMapperImpl() }
        single<ContributionUiMapper> { ContributionUiMapperImpl() }
        single<RoomUiMapper> { RoomUiMapperImpl(get(), get()) }
        single<SavedStoryUiMapper> { SavedStoryUiMapperImpl(get(), get()) }
        single<StoryHistoryStore> { SettingsStoryHistoryStore(Settings()) }
        single<GameRepository> {
            RemoteGameRepository(
                serverUrl = get<ServerUrlProvider>().serverUrl(),
                client = get(),
                sessionStore = get(),
                storyHistoryStore = get(),
                reconnectPolicy = get(),
                sessionReducer = get(),
                connectionManager = get(),
                timeProvider = get(),
            )
        }
        factory<ConnectToGameUseCase> { ConnectToGameUseCaseImpl(get()) }
        factory<CreateRoomUseCase> { CreateRoomUseCaseImpl(get()) }
        factory<JoinRoomUseCase> { JoinRoomUseCaseImpl(get()) }
        factory<StartGameUseCase> { StartGameUseCaseImpl(get()) }
        factory<SubmitPromptUseCase> { SubmitPromptUseCaseImpl(get()) }
        factory<SubmitContributionUseCase> { SubmitContributionUseCaseImpl(get()) }
        factory<PlayAgainUseCase> { PlayAgainUseCaseImpl(get()) }
        factory<LoadStoryHistoryUseCase> { LoadStoryHistoryUseCaseImpl(get()) }
        factory<OpenSavedStoryUseCase> { OpenSavedStoryUseCaseImpl(get()) }
        factory { AppViewModel(get(), get()) }
        factory { HomeViewModel(get(), get(), get(), get()) }
        factory { LobbyViewModel(get(), get(), get(), get()) }
        factory { StoryViewModel(get(), get(), get(), get(), get(), get(), get()) }
        factory { FinishedViewModel(get(), get(), get()) }
        factory { HistoryViewModel(get(), get(), get(), get()) }
        factory { SavedStoryViewModel(get(), get()) }
    }
