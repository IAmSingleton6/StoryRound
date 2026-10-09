package com.example.storyround.data.local

import com.example.storyround.protocol.SavedStory

internal interface StoryHistoryStore {
    fun load(): List<SavedStory>

    fun save(story: SavedStory)
}
