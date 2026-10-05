package com.example.storyround.persistence

import org.jetbrains.exposed.v1.core.Table

internal class StorySchema {
    val stories = StoriesTable()
    val players = StoryPlayersTable(stories)
    val contributions = StoryContributionsTable(stories)

    val tables: Array<Table> = arrayOf(stories, players, contributions)
}
