package com.example.storyround.testing

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlin.test.assertEquals

class BrowserRobot(
    private val server: StoryRoundTestServer,
) {
    private var client: HttpClient? = null
    private var lastResponse: HttpResponse? = null

    fun open() {
        client = server.createClient()
    }

    suspend fun visit(path: String) {
        lastResponse = checkNotNull(client) { "Open the browser before visiting a page." }.get(path)
    }

    suspend fun assertAnswered(
        status: HttpStatusCode,
        body: String,
    ) {
        val response = checkNotNull(lastResponse) { "Visit a page before checking its answer." }
        assertEquals(status, response.status)
        assertEquals(body, response.bodyAsText())
    }
}
