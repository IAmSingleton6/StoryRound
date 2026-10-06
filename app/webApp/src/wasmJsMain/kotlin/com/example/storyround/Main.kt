package com.example.storyround

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.example.storyround.di.initializeStoryRound

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initializeStoryRound(WebServerUrlProvider())
    ComposeViewport { App() }
}
