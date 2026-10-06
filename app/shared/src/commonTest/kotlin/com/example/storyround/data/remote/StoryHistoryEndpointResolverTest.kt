package com.example.storyround.data.remote

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StoryHistoryEndpointResolverTest {
    private val resolver = StoryHistoryEndpointResolver()

    @Test
    fun `given websocket game URLs when resolving history then HTTP endpoints are produced`() {
        assertEquals("http://localhost:8080/stories", resolver.resolve("ws://localhost:8080/game", "/stories"))
        assertEquals("https://example.com/stories/story-1", resolver.resolve("wss://example.com/game", "/stories/story-1"))
    }

    @Test
    fun `given an invalid path when resolving then validation fails`() {
        assertFailsWith<IllegalArgumentException> {
            resolver.resolve("ws://localhost:8080/game", "stories")
        }
    }
}
