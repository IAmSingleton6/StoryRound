package com.example.storyround

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.example.storyround.di.initializeStoryRound
import com.example.storyround.logging.LoggingConfiguration

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initializeStoryRound(WebServerUrlProvider(), LoggingConfiguration.Release)
    ComposeViewport { App() }
}
