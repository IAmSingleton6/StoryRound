package com.example.storyround

import com.example.storyround.configuration.SERVER_URL_CONFIGURATION_KEY
import com.example.storyround.configuration.ServerUrlProvider
import platform.Foundation.NSBundle

internal class BundleServerUrlProvider : ServerUrlProvider {
    override fun serverUrl(): String =
        requireNotNull(
            NSBundle.mainBundle.objectForInfoDictionaryKey(SERVER_URL_CONFIGURATION_KEY) as? String,
        ) { "$SERVER_URL_CONFIGURATION_KEY must be set in the app configuration." }
}
