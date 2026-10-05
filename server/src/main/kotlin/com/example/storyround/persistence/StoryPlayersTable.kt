package com.example.storyround.persistence

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

internal class StoryPlayersTable(
    stories: StoriesTable,
) : Table("story_players") {
    val storyId = reference("story_id", stories.id, onDelete = ReferenceOption.CASCADE)
    val playerId = varchar("player_id", 36)
    val name = varchar("name", 100)
    val colour = varchar("colour", 16)
    override val primaryKey = PrimaryKey(storyId, playerId)
}
