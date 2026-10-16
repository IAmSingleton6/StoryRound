package com.example.storyround

import android.app.Application
import com.example.storyround.di.initializeStoryRound
import com.example.storyround.logging.LoggingConfiguration

class StoryRoundApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeStoryRound(
            serverUrlProvider = AndroidServerUrlProvider(),
            loggingConfiguration = if (BuildConfig.DEBUG) LoggingConfiguration.Development else LoggingConfiguration.Release,
        )
    }
}
