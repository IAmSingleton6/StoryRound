package com.example.storyround.data.idea

import com.example.storyround.domain.idea.StoryIdeaProvider
import com.example.storyround.random.RandomNumberGenerator
import org.jetbrains.compose.resources.StringArrayResource
import org.jetbrains.compose.resources.getStringArray
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.story_ideas_new_sentence
import storyround.app.shared.generated.resources.story_ideas_opening
import storyround.app.shared.generated.resources.story_ideas_sentence_ending

internal class BundledStoryIdeaProvider(
    private val randomNumberGenerator: RandomNumberGenerator,
) : StoryIdeaProvider {
    override suspend fun openingIdea(current: String): String = Res.array.story_ideas_opening.pickOtherThan(current)

    override suspend fun continuationIdea(
        storySoFar: String,
        current: String,
    ): String {
        val ideas = if (storySoFar.endsSentence()) Res.array.story_ideas_new_sentence else Res.array.story_ideas_sentence_ending
        return ideas.pickOtherThan(current)
    }

    private fun String.endsSentence(): Boolean {
        val trimmed = trimEnd().trimEnd(*CLOSING_QUOTES)
        return trimmed.isEmpty() || trimmed.last() in SENTENCE_TERMINATORS
    }

    private suspend fun StringArrayResource.pickOtherThan(current: String): String =
        getStringArray(this)
            .filter { it != current }
            .let { it[randomNumberGenerator.nextInt(it.size)] }
}

private val SENTENCE_TERMINATORS = setOf('.', '!', '?', '…')

private val CLOSING_QUOTES = charArrayOf('"', '\'', '”', '’', ')')
