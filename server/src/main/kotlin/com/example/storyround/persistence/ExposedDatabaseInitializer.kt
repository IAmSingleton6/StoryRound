package com.example.storyround.persistence

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

internal class ExposedDatabaseInitializer(
    private val schema: StorySchema,
) : DatabaseInitializer {
    override fun connect(url: String) {
        Database.connect(url, driver = "org.postgresql.Driver")
        transaction {
            SchemaUtils.create(*schema.tables)
        }
    }
}
