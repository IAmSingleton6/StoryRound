package com.example.storyround.persistence

import com.example.storyround.identity.IdentifierGenerator

internal class PostgresStoryRepositoryProvider(
    private val identifierGenerator: IdentifierGenerator,
) : StoryRepositoryProvider {
    override fun provide(databaseUrl: String?): StoryRepository? =
        databaseUrl?.let { url ->
            val schema = StorySchema()
            ExposedDatabaseInitializer(schema).connect(url)
            PostgresStoryRepository(schema, identifierGenerator)
        }
}
