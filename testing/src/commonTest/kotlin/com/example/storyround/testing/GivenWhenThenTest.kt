package com.example.storyround.testing

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class GivenWhenThenTest {
    @Test
    fun `should run each step's body when the steps run`() =
        runTest {
            // Given
            val ran = mutableListOf<String>()

            // When
            GIVEN("first") { ran += "GIVEN" }
            WHEN("second") {
                yield()
                ran += "WHEN"
            }
            THEN("third") { ran += "THEN" }
            AND("fourth") { ran += "AND" }

            // Then
            assertEquals(listOf("GIVEN", "WHEN", "THEN", "AND"), ran)
        }

    @Test
    fun `should let a step's failure through unchanged when the step fails`() {
        // Given
        val failure = AssertionError("Sam is missing")

        // When
        val thrown = runCatching { THEN("the lobby shows both players") { throw failure } }.exceptionOrNull()

        // Then
        assertSame(failure, thrown)
    }
}
