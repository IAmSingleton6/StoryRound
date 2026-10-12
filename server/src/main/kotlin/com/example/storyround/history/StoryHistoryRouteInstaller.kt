package com.example.storyround.history

import com.example.storyround.persistence.StoryRepositoryAvailability
import com.example.storyround.protocol.SavedStory
import com.example.storyround.protocol.StorySummary
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

internal class StoryHistoryRouteInstaller(
    private val persistence: StoryRepositoryAvailability,
    private val json: Json,
) {
    fun install(route: Route) {
        route.get("/stories") {
            val repository = persistence.repository ?: return@get call.respondHistoryUnavailable()
            val stories =
                runCatching { repository.list() }
                    .getOrElse { return@get call.respondHistoryUnavailable() }
            call.respondJson(stories, ListSerializer(StorySummary.serializer()))
        }
        route.get("/stories/{id}") {
            val repository = persistence.repository ?: return@get call.respondHistoryUnavailable()
            val story =
                runCatching { repository.find(requireNotNull(call.parameters["id"])) }
                    .getOrElse { return@get call.respondHistoryUnavailable() }
                    ?: return@get call.respondText("Story not found.", ContentType.Text.Plain, HttpStatusCode.NotFound)
            call.respondJson(story, SavedStory.serializer())
        }
    }

    private suspend fun ApplicationCall.respondHistoryUnavailable() {
        respondText(
            "Story history is unavailable because persistence is not configured or reachable.",
            ContentType.Text.Plain,
            HttpStatusCode.ServiceUnavailable,
        )
    }

    private suspend fun <T> ApplicationCall.respondJson(
        value: T,
        serializer: KSerializer<T>,
    ) {
        respondText(json.encodeToString(serializer, value), ContentType.Application.Json)
    }
}
