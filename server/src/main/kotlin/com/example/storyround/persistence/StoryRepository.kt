package com.example.storyround.persistence

import com.example.storyround.protocol.SavedStory
import com.example.storyround.protocol.StorySummary

internal interface StoryRepository {
    fun save(story: SavedStory)

    fun find(id: String): SavedStory?

    fun list(): List<StorySummary>
}
