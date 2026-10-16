package com.example.storyround.data.local

import kotlinx.serialization.json.Json

internal fun storageJson(): Json = Json { ignoreUnknownKeys = true }
