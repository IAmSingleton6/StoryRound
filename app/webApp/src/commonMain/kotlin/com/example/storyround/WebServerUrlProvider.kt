package com.example.storyround

import com.example.storyround.configuration.ServerUrlProvider

internal class WebServerUrlProvider : ServerUrlProvider {
    override fun serverUrl(): String = WEB_SERVER_URL
}
