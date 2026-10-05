package com.example.storyround

import com.example.storyround.configuration.ServerUrlProvider

internal class AndroidServerUrlProvider : ServerUrlProvider {
    override fun serverUrl(): String = BuildConfig.STORY_ROUND_SERVER_URL
}
