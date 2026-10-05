package com.example.storyround

import com.example.storyround.configuration.ServerUrlProvider

internal class DesktopServerUrlProvider : ServerUrlProvider {
    override fun serverUrl(): String = DESKTOP_SERVER_URL
}
