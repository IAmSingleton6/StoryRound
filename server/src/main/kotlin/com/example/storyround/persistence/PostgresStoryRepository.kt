package com.example.storyround.persistence

import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.SavedStory
import com.example.storyround.protocol.StorySummary
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

internal class PostgresStoryRepository(
    private val schema: StorySchema,
) : StoryRepository {
    override fun save(story: SavedStory) {
        transaction {
            schema.stories.insert { row ->
                row[id] = story.id
                row[schema.stories.prompt] = story.prompt
                row[schema.stories.promptPlayerId] = story.promptPlayerId
                row[schema.stories.createdAtEpochMillis] = story.createdAtEpochMillis
            }
            story.players.forEach { player ->
                schema.players.insert { row ->
                    row[storyId] = story.id
                    row[playerId] = player.id
                    row[name] = player.name
                    row[colour] = player.colour.name
                }
            }
            story.contributions.forEach { contribution ->
                schema.contributions.insert { row ->
                    row[storyId] = story.id
                    row[turnNumber] = contribution.turnNumber
                    row[playerId] = contribution.playerId
                    row[text] = contribution.text
                }
            }
        }
    }

    override fun find(id: String): SavedStory? =
        transaction {
            val story =
                schema.stories
                    .selectAll()
                    .where { schema.stories.id eq id }
                    .singleOrNull() ?: return@transaction null
            val players =
                schema.players.selectAll().where { schema.players.storyId eq id }.map { row ->
                    Player(row[schema.players.playerId], row[schema.players.name], PlayerColour.valueOf(row[schema.players.colour]))
                }
            val contributions =
                schema.contributions
                    .selectAll()
                    .where { schema.contributions.storyId eq id }
                    .orderBy(schema.contributions.turnNumber to SortOrder.ASC)
                    .map { row ->
                        Contribution(
                            row[schema.contributions.playerId],
                            row[schema.contributions.text],
                            row[schema.contributions.turnNumber],
                        )
                    }
            SavedStory(
                id = id,
                prompt = story[schema.stories.prompt],
                promptPlayerId = story[schema.stories.promptPlayerId],
                createdAtEpochMillis = story[schema.stories.createdAtEpochMillis],
                players = players,
                contributions = contributions,
            )
        }

    override fun list(): List<StorySummary> =
        transaction {
            schema.stories.selectAll().orderBy(schema.stories.createdAtEpochMillis to SortOrder.DESC).map { row ->
                val storyId = row[schema.stories.id]
                StorySummary(
                    id = storyId,
                    prompt = row[schema.stories.prompt],
                    createdAtEpochMillis = row[schema.stories.createdAtEpochMillis],
                    playerCount =
                        schema.players
                            .selectAll()
                            .where { schema.players.storyId eq storyId }
                            .count()
                            .toInt(),
                    contributionCount =
                        schema.contributions
                            .selectAll()
                            .where { schema.contributions.storyId eq storyId }
                            .count()
                            .toInt(),
                )
            }
        }
}
