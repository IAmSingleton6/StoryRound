package com.example.storyround.domain.idea

internal interface StoryIdeaProvider {
    fun openingIdea(current: String): String

    fun continuationIdea(
        storySoFar: String,
        current: String,
    ): String
}
