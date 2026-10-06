package com.example.storyround

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.storyround.di.initializeStoryRound

fun main() {
    initializeStoryRound(DesktopServerUrlProvider())
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Story Round",
        ) {
            App()
        }
    }
}
