package com.example.storyround

import com.example.storyround.persistence.StoryRepository
import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.SavedStory
import com.example.storyround.protocol.StorySummary
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testRoot() =
        testApplication {
            application {
                module()
            }
            val response = client.get("/")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Story Relay server", response.bodyAsText())
        }

    @Test
    fun `given saved stories when listing history then summaries are returned newest first`() =
        testApplication {
            val repository =
                mockk<StoryRepository> {
                    every { list() } returns
                        listOf(summary(id = "newer", createdAtEpochMillis = 20), summary(id = "older", createdAtEpochMillis = 10))
                }
            application { module(storyRepository = repository) }

            val response = client.get("/stories")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(
                listOf("newer", "older"),
                Json.decodeFromString(ListSerializer(StorySummary.serializer()), response.bodyAsText()).map { it.id },
            )
        }

    @Test
    fun `given a saved story when requesting it then its complete ordered content is returned`() =
        testApplication {
            val story = savedStory()
            application { module(storyRepository = mockk { every { find(story.id) } returns story }) }

            val response = client.get("/stories/${story.id}")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(story, Json.decodeFromString(SavedStory.serializer(), response.bodyAsText()))
        }

    @Test
    fun `given persistence is unavailable when requesting history then service unavailable is returned`() =
        testApplication {
            application { module() }

            val response = client.get("/stories")

            assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        }

    @Test
    fun `given configured persistence fails when listing history then service unavailable is returned`() =
        testApplication {
            application { module(storyRepository = mockk { every { list() } throws IllegalStateException("Database connection lost") }) }

            val response = client.get("/stories")

            assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        }

    @Test
    fun `given configured persistence fails when loading a story then service unavailable is returned`() =
        testApplication {
            application {
                module(
                    storyRepository = mockk { every { find(any()) } throws IllegalStateException("Database connection lost") },
                )
            }

            val response = client.get("/stories/story-1")

            assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        }

    @Test
    fun `given an unknown story when requesting it then not found is returned`() =
        testApplication {
            application { module(storyRepository = mockk { every { find("missing") } returns null }) }

            val response = client.get("/stories/missing")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    private fun savedStory() =
        SavedStory(
            id = "story-1",
            prompt = "Prompt",
            promptPlayerId = "player-1",
            createdAtEpochMillis = 10,
            players = listOf(Player("player-1", "Alex", PlayerColour.Blue)),
            contributions = listOf(Contribution("player-1", "Ending.", 0)),
        )

    private fun summary(
        id: String,
        createdAtEpochMillis: Long,
    ) = StorySummary(id = id, prompt = "Prompt", createdAtEpochMillis = createdAtEpochMillis, playerCount = 1, contributionCount = 1)
}
