package com.example.storyround

import android.app.Application
import com.example.storyround.di.initializeStoryRound

class StoryRoundApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeStoryRound(AndroidServerUrlProvider())
    }
}
