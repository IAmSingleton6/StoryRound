package com.example.storyround.data.idea

import com.example.storyround.protocol.MAX_STORY_TEXT_LENGTH
import com.example.storyround.random.RandomNumberGenerator
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BundledStoryIdeaProviderTest {
    private var nextIndex = 0

    private val randomNumberGenerator = mockk<RandomNumberGenerator> { every { nextInt(any()) } answers { nextIndex++ % firstArg<Int>() } }
    private val provider = BundledStoryIdeaProvider(randomNumberGenerator)

    @Test
    fun `given the bundled ideas when one is requested then it comes from the resource with its punctuation intact`() =
        runTest {
            // Given
            nextIndex = SHADOWS_VOICE_INDEX

            // When
            val idea = provider.continuationIdea(storySoFar = "The lights went out.", current = "")

            // Then
            assertEquals("A voice from the shadows whispered, \"You're late.\"", idea)
        }

    @Test
    fun `given the current idea when another is requested then a different idea is returned`() =
        runTest {
            // When
            val openings =
                List(50) {
                    provider.openingIdea(current = "").let { current ->
                        current to provider.openingIdea(current = current)
                    }
                }
            val continuations =
                List(50) {
                    provider.continuationIdea(storySoFar = "", current = "").let { current ->
                        current to provider.continuationIdea(storySoFar = "", current = current)
                    }
                }

            // Then
            (openings + continuations).forEach { (current, next) -> assertNotEquals(current, next) }
        }

    @Test
    fun `given any idea when returned then it fits in a single contribution`() =
        runTest {
            // When
            val ideas =
                List(50) { provider.openingIdea(current = "") } +
                    List(50) { provider.continuationIdea(storySoFar = "It began", current = "") }

            // Then
            ideas.forEach { idea -> assertTrue(idea.length <= MAX_STORY_TEXT_LENGTH, idea) }
        }

    @Test
    fun `given a story ending a sentence when a continuation is requested then it starts a new sentence`() =
        runTest {
            // Given
            val stories = listOf("A door opened.", "Run!", "Who's there? ", "She said \"Stop.\"")

            // When
            val continuations = stories.map { story -> provider.continuationIdea(storySoFar = story, current = "") }

            // Then
            continuations.forEach { continuation -> assertTrue(continuation.first().isUpperCase(), continuation) }
        }

    @Test
    fun `given a story mid sentence when a continuation is requested then it finishes the sentence`() =
        runTest {
            // Given
            val stories = listOf("A door opened", "The horse, wearing a hat,", "They ran towards the")

            // When
            val continuations = stories.map { story -> provider.continuationIdea(storySoFar = story, current = "") }

            // Then
            continuations.forEach { continuation -> assertTrue(continuation.first().isLowerCase(), continuation) }
        }

    private companion object {
        const val SHADOWS_VOICE_INDEX = 8
    }
}
