package com.example.storyround.di

import com.example.storyround.configuration.ServerUrlProvider
import com.example.storyround.logging.LoggingConfiguration
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun initializeStoryRound(
    serverUrlProvider: ServerUrlProvider,
    loggingConfiguration: LoggingConfiguration,
) {
    startKoin {
        modules(
            appModule(),
            module {
                single<ServerUrlProvider> { serverUrlProvider }
                single { loggingConfiguration }
            },
        )
    }
}
