package com.example.storyround

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.storyround.di.initializeStoryRound
import com.example.storyround.logging.LoggingConfiguration

fun main() {
    initializeStoryRound(DesktopServerUrlProvider(), LoggingConfiguration.Release)
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Story Round",
        ) {
            App()
        }
    }
}
