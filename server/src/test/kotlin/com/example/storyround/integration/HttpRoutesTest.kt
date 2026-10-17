package com.example.storyround.integration

import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import io.ktor.http.HttpStatusCode
import kotlin.test.Test

class HttpRoutesTest : ServerIntegrationTest() {
    @Test
    fun `should answer with the server's name when the root path is requested`() =
        serverTest {
            GIVEN("a plain HTTP client") {
                browserRobot.open()
            }

            WHEN("a browser requests the root path") {
                browserRobot.visit("/")
            }

            THEN("the server answers OK with its name") {
                browserRobot.assertAnswered(HttpStatusCode.OK, "Story Relay server")
            }
        }
}
