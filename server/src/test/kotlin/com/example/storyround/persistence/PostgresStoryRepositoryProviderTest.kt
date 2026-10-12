package com.example.storyround.persistence

import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNull

class PostgresStoryRepositoryProviderTest {
    private val databaseInitializer = mockk<DatabaseInitializer>(relaxed = true)
    private val provider = PostgresStoryRepositoryProvider(databaseInitializer, StorySchema())

    @Test
    fun `given no database url when provided then the server runs without persistence`() {
        assertNull(provider.provide(databaseUrl = null))
        verify(exactly = 0) { databaseInitializer.connect(any()) }
    }

    @Test
    fun `given a database url when provided then the database is connected and a repository returned`() {
        val repository = provider.provide(databaseUrl = "jdbc:postgresql://localhost/story_round")

        assertIs<PostgresStoryRepository>(repository)
        verify(exactly = 1) { databaseInitializer.connect("jdbc:postgresql://localhost/story_round") }
    }
}
