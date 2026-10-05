package com.example.storyround.persistence

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

internal class StoryContributionsTable(
    stories: StoriesTable,
) : Table("story_contributions") {
    val storyId = reference("story_id", stories.id, onDelete = ReferenceOption.CASCADE)
    val turnNumber = integer("turn_number")
    val playerId = varchar("player_id", 36)
    val text = varchar("text", 280)
    override val primaryKey = PrimaryKey(storyId, turnNumber)
}
