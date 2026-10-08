package com.example.storyround

import com.example.storyround.persistence.StoryRepository
import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.SavedStory
import com.example.storyround.protocol.StorySummary
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
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
                TestStoryRepository(
                    listOf(
                        savedStory(id = "newer", prompt = "Newer story", createdAtEpochMillis = 20),
                        savedStory(id = "older", prompt = "Older story", createdAtEpochMillis = 10),
                    ),
                )
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
            application { module(storyRepository = TestStoryRepository(listOf(story))) }

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
            application { module(storyRepository = FailingStoryRepository()) }

            val response = client.get("/stories")

            assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        }

    @Test
    fun `given configured persistence fails when loading a story then service unavailable is returned`() =
        testApplication {
            application { module(storyRepository = FailingStoryRepository()) }

            val response = client.get("/stories/story-1")

            assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        }

    @Test
    fun `given an unknown story when requesting it then not found is returned`() =
        testApplication {
            application { module(storyRepository = TestStoryRepository(emptyList())) }

            val response = client.get("/stories/missing")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    private fun savedStory(
        id: String = "story-1",
        prompt: String = "Prompt",
        createdAtEpochMillis: Long = 10,
    ) = SavedStory(
        id = id,
        prompt = prompt,
        promptPlayerId = "player-1",
        createdAtEpochMillis = createdAtEpochMillis,
        players = listOf(Player("player-1", "Alex", PlayerColour.Blue)),
        contributions = listOf(Contribution("player-1", "Ending.", 0)),
    )

    private class TestStoryRepository(
        stories: List<SavedStory>,
    ) : StoryRepository {
        private val stories = stories.associateBy { it.id }.toMutableMap()

        override fun save(
            room: RoomState,
            createdAtEpochMillis: Long,
        ): SavedStory = error("Not used by this test.")

        override fun find(id: String): SavedStory? = stories[id]

        override fun list(): List<StorySummary> =
            stories.values
                .sortedByDescending { it.createdAtEpochMillis }
                .map { story ->
                    StorySummary(
                        id = story.id,
                        prompt = story.prompt,
                        createdAtEpochMillis = story.createdAtEpochMillis,
                        playerCount = story.players.size,
                        contributionCount = story.contributions.size,
                    )
                }
    }

    private class FailingStoryRepository : StoryRepository {
        override fun save(
            room: RoomState,
            createdAtEpochMillis: Long,
        ): SavedStory = error("Not used by this test.")

        override fun find(id: String): SavedStory? = error("Database connection lost")

        override fun list(): List<StorySummary> = error("Database connection lost")
    }
}
