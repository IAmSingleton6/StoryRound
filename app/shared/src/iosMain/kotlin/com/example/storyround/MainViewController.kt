package com.example.storyround

import androidx.compose.ui.window.ComposeUIViewController
import com.example.storyround.di.initializeStoryRound

@Suppress("ktlint:standard:function-naming")
fun MainViewController(): ComposeUIViewController {
    initializeStoryRound(BundleServerUrlProvider())
    return ComposeUIViewController { App() }
}
