package com.example.storyround.persistence

internal class PostgresStoryRepositoryProvider(
    private val databaseInitializer: DatabaseInitializer,
    private val schema: StorySchema,
) : StoryRepositoryProvider {
    override fun provide(databaseUrl: String?): StoryRepository? =
        databaseUrl?.let { url ->
            databaseInitializer.connect(url)
            PostgresStoryRepository(schema)
        }
}
