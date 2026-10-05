package com.example.storyround.persistence

internal interface DatabaseInitializer {
    fun connect(url: String)
}
