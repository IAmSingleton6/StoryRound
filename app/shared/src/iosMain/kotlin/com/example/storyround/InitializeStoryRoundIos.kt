package com.example.storyround

import com.example.storyround.di.initializeStoryRound
import com.example.storyround.logging.LoggingConfiguration
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

@OptIn(ExperimentalNativeApi::class)
fun initializeStoryRoundIos() {
    initializeStoryRound(
        serverUrlProvider = BundleServerUrlProvider(),
        loggingConfiguration = if (Platform.isDebugBinary) LoggingConfiguration.Development else LoggingConfiguration.Release,
    )
}
