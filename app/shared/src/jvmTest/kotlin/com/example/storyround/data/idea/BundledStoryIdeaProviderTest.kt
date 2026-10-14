package com.example.storyround.data.idea

import com.example.storyround.protocol.MAX_STORY_TEXT_LENGTH
import com.example.storyround.random.RandomNumberGenerator
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BundledStoryIdeaProviderTest {
    private var nextIndex = 0

    private val randomNumberGenerator = mockk<RandomNumberGenerator> { every { nextInt(any()) } answers { nextIndex++ % firstArg<Int>() } }
    private val provider = BundledStoryIdeaProvider(randomNumberGenerator)

    @Test
    fun `given the current idea when another is requested then a different idea is returned`() {
        repeat(50) {
            val opening = provider.openingIdea(current = "")
            assertNotEquals(opening, provider.openingIdea(current = opening))

            val continuation = provider.continuationIdea(storySoFar = "", current = "")
            assertNotEquals(continuation, provider.continuationIdea(storySoFar = "", current = continuation))
        }
    }

    @Test
    fun `given any idea when returned then it fits in a single contribution`() {
        repeat(50) {
            assertTrue(provider.openingIdea(current = "").length <= MAX_STORY_TEXT_LENGTH)
            assertTrue(provider.continuationIdea(storySoFar = "It began", current = "").length <= MAX_STORY_TEXT_LENGTH)
        }
    }

    @Test
    fun `given a story ending a sentence when a continuation is requested then it starts a new sentence`() {
        listOf("A door opened.", "Run!", "Who's there? ", "She said \"Stop.\"").forEach { story ->
            assertTrue(provider.continuationIdea(storySoFar = story, current = "").first().isUpperCase())
        }
    }

    @Test
    fun `given a story mid sentence when a continuation is requested then it finishes the sentence`() {
        listOf("A door opened", "The horse, wearing a hat,", "They ran towards the").forEach { story ->
            assertTrue(provider.continuationIdea(storySoFar = story, current = "").first().isLowerCase())
        }
    }
}
