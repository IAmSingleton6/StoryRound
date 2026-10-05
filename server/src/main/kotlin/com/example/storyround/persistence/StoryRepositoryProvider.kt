package com.example.storyround.persistence

internal interface StoryRepositoryProvider {
    fun provide(databaseUrl: String?): StoryRepository?
}
