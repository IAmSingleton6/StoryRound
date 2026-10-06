package com.example.storyround.data.remote

import com.example.storyround.protocol.SavedStory
import com.example.storyround.protocol.StorySummary
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

internal class KtorStoryHistoryDataSource(
    private val client: HttpClient,
    private val json: Json = Json { classDiscriminator = "type" },
    private val endpointResolver: StoryHistoryEndpointResolver = StoryHistoryEndpointResolver(),
) : StoryHistoryDataSource {
    override suspend fun loadStories(serverUrl: String): List<StorySummary> {
        val response = client.get(endpointResolver.resolve(serverUrl, "/stories"))
        require(response.status.value in 200..299) {
            "Could not load story history (${response.status.value})."
        }
        return json.decodeFromString(ListSerializer(StorySummary.serializer()), response.bodyAsText())
    }

    override suspend fun loadStory(
        serverUrl: String,
        id: String,
    ): SavedStory {
        val response = client.get(endpointResolver.resolve(serverUrl, "/stories/$id"))
        require(response.status.value in 200..299) {
            "Could not load this story (${response.status.value})."
        }
        return json.decodeFromString(SavedStory.serializer(), response.bodyAsText())
    }
}
