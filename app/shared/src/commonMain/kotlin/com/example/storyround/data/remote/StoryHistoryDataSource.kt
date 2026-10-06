package com.example.storyround.data.remote

import com.example.storyround.protocol.SavedStory
import com.example.storyround.protocol.StorySummary

internal interface StoryHistoryDataSource {
    suspend fun loadStories(serverUrl: String): List<StorySummary>

    suspend fun loadStory(
        serverUrl: String,
        id: String,
    ): SavedStory
}
