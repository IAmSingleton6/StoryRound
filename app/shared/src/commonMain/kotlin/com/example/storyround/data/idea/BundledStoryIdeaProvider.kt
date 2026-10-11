package com.example.storyround.data.idea

import com.example.storyround.domain.idea.StoryIdeaProvider
import com.example.storyround.random.RandomNumberGenerator

internal class BundledStoryIdeaProvider(
    private val randomNumberGenerator: RandomNumberGenerator,
) : StoryIdeaProvider {
    override fun openingIdea(current: String): String = OPENING_IDEAS.pickOtherThan(current)

    override fun continuationIdea(
        storySoFar: String,
        current: String,
    ): String {
        val ideas = if (storySoFar.endsSentence()) NEW_SENTENCE_IDEAS else SENTENCE_ENDING_IDEAS
        return ideas.pickOtherThan(current)
    }

    private fun String.endsSentence(): Boolean {
        val trimmed = trimEnd().trimEnd(*CLOSING_QUOTES)
        return trimmed.isEmpty() || trimmed.last() in SENTENCE_TERMINATORS
    }

    private fun List<String>.pickOtherThan(current: String): String =
        filter {
            it != current
        }.let { it[randomNumberGenerator.nextInt(it.size)] }
}

private val OPENING_IDEAS =
    listOf(
        "The lighthouse keeper had not seen a ship in forty years, until this morning.",
        "Every cat in the village disappeared on the same night.",
        "My grandmother's recipe book had one page that was always locked.",
        "The new kid at school claimed to be from the year 2300.",
        "At exactly midnight, the museum's statues began to argue.",
        "The treasure map was drawn on the back of a takeaway menu.",
        "Nobody remembered inviting the man in the purple hat to the wedding.",
        "The last dragon in the world was working at a car wash.",
        "When the elevator stopped between floors, a voice asked for the password.",
        "The robot vacuum had been collecting something other than dust.",
        "It rained upwards in the town of Little Wickham every Tuesday.",
        "A letter arrived addressed to me, postmarked fifty years in the future.",
        "The goldfish won the lottery.",
        "Deep beneath the library, someone was still reading by candlelight.",
        "The astronauts returned home to find the Earth had been redecorated.",
        "On the first day of her new job, the witch accidentally turned her boss into a teapot.",
        "The snowman in the garden was in a different place every morning.",
        "Captain Barnacle's ship had a leak, a mutiny, and a very confused parrot.",
        "The haunted house was for sale, and the ghost was the estate agent.",
        "Every mirror in town started showing tomorrow.",
    )

private val SENTENCE_TERMINATORS = setOf('.', '!', '?', '…')

private val CLOSING_QUOTES = charArrayOf('"', '\'', '”', '’', ')')

private val NEW_SENTENCE_IDEAS =
    listOf(
        "Suddenly, the lights went out.",
        "Nobody noticed the door quietly closing behind them.",
        "Meanwhile, on the other side of town, an alarm started ringing.",
        "Just when things seemed hopeless, a taxi pulled up.",
        "Without warning, the ground began to shake.",
        "To everyone's surprise, the dog started talking.",
        "Then came a knock at the door.",
        "Unfortunately, nobody had read the instructions.",
        "A voice from the shadows whispered, \"You're late.\"",
        "In a panic, they grabbed the nearest umbrella and ran.",
        "It turned out the whole thing had been a birthday surprise.",
        "Hours later, still covered in glitter, they reached the top.",
        "The plan was perfect, except for the goat.",
        "Out of nowhere, a flock of pigeons swooped down.",
        "Against all advice, they pressed the big red button.",
        "With a deep breath and a terrible idea, they began to sing.",
        "Little did they know, they were being followed.",
        "The next morning, everything was purple.",
        "Somewhere far below, something enormous woke up.",
        "But the strangest part was the note left on the table.",
    )

private val SENTENCE_ENDING_IDEAS =
    listOf(
        "when suddenly the lights went out.",
        "and nobody noticed the door quietly closing behind them.",
        "until a taxi pulled up out of nowhere.",
        "just as the ground began to shake.",
        "but then the dog started talking.",
        "because nobody had read the instructions.",
        "while a voice from the shadows whispered, \"You're late.\"",
        "and that was when the pigeons arrived.",
        "except for one small problem: the goat.",
        "so they pressed the big red button, against all advice.",
        "and by the next morning, everything was purple.",
        "which woke something enormous far below.",
        "before anyone could ask what the smell was.",
        "and the whole thing turned out to be a birthday surprise.",
        "without realising they were being followed.",
        "until someone found the note left on the table.",
        "and then, with a terrible idea, they began to sing.",
        "only to discover it was covered in glitter.",
        "as the clock struck thirteen.",
        "which, frankly, explained the umbrella.",
    )
