package com.example.storyround.persistence

internal class PostgresStoryRepositoryProvider : StoryRepositoryProvider {
    override fun provide(databaseUrl: String?): StoryRepository? =
        databaseUrl?.let { url ->
            val schema = StorySchema()
            ExposedDatabaseInitializer(schema).connect(url)
            PostgresStoryRepository(schema)
        }
}
