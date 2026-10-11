package com.example.storyround

import androidx.compose.ui.window.ComposeUIViewController
import com.example.storyround.di.initializeStoryRound
import platform.UIKit.UIViewController

@Suppress("ktlint:standard:function-naming")
fun MainViewController(): UIViewController {
    initializeStoryRound(BundleServerUrlProvider())
    return ComposeUIViewController { App() }
}
