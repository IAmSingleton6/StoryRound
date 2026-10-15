package com.example.storyround.domain.idea

internal interface StoryIdeaProvider {
    suspend fun openingIdea(current: String): String

    suspend fun continuationIdea(
        storySoFar: String,
        current: String,
    ): String
}
