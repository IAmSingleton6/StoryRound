package com.example.storyround.di

import com.example.storyround.configuration.ServerUrlProvider
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

fun initializeStoryRound(serverUrlProvider: ServerUrlProvider) {
    if (KoinPlatform.getKoinOrNull() != null) return

    startKoin {
        modules(
            appModule,
            module {
                single<ServerUrlProvider> { serverUrlProvider }
            },
        )
    }
}
