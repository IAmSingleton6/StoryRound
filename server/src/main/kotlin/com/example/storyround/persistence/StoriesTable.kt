package com.example.storyround.persistence

import org.jetbrains.exposed.v1.core.Table

internal class StoriesTable : Table("stories") {
    val id = varchar("id", 36)
    val prompt = varchar("prompt", 280)
    val createdAtEpochMillis = long("created_at_epoch_millis")
    override val primaryKey = PrimaryKey(id)
}
