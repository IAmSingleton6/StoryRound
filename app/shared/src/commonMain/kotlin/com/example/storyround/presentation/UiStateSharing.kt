package com.example.storyround.presentation

import kotlinx.coroutines.flow.SharingStarted

internal val WhileUiSubscribed: SharingStarted = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000)
