package com.example.storyround.di

import com.example.storyround.persistence.StoryRepository

internal class StoryRepositoryAvailability(
    val repository: StoryRepository?,
)
