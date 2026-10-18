package com.example.storyround.protocol

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserTextValidatorImplTest {
    private val validator = UserTextValidatorImpl(UserTextSanitizerImpl())
    private val freeWriting = RoomSettings(rounds = 3)
    private val oneWord = RoomSettings(rounds = 3, oneWordPerTurn = true)

    @Test
    fun `given a padded name when validated then the sanitised name is returned`() {
        // When
        val name = validator.validPlayerName("  Mary \n Jane ")

        // Then
        assertEquals("Mary Jane", name)
    }

    @Test
    fun `given a name too short after sanitising or too long when validated then it is rejected`() {
        // When
        val tooShort = validator.validPlayerName(" A​ ")
        val tooLong = validator.validPlayerName("A".repeat(MAX_PLAYER_NAME_LENGTH + 1))

        // Then
        assertNull(tooShort)
        assertNull(tooLong)
    }

    @Test
    fun `given story text within the limit when validated then the sanitised text is returned`() {
        // When
        val text = validator.validStoryText(" A door\nopened. ", freeWriting)
        val lengthAtLimit = validator.validStoryText("a".repeat(MAX_STORY_TEXT_LENGTH), freeWriting)?.length

        // Then
        assertEquals("A door opened.", text)
        assertEquals(MAX_STORY_TEXT_LENGTH, lengthAtLimit)
    }

    @Test
    fun `given blank or over-long story text when validated then it is rejected`() {
        // When
        val blank = validator.validStoryText(" ​\n ", freeWriting)
        val overLong = validator.validStoryText("a".repeat(MAX_STORY_TEXT_LENGTH + 1), freeWriting)

        // Then
        assertNull(blank)
        assertNull(overLong)
    }

    @Test
    fun `given one word turns and a padded word with punctuation when validated then the sanitised word is returned`() {
        // When
        val quoted = validator.validStoryText("  \"Hello, \n", oneWord)
        val hyphenated = validator.validStoryText("mother-in-law's", oneWord)
        val lengthAtLimit = validator.validStoryText("a".repeat(MAX_WORD_LENGTH), oneWord)?.length

        // Then
        assertEquals("\"Hello,", quoted)
        assertEquals("mother-in-law's", hyphenated)
        assertEquals(MAX_WORD_LENGTH, lengthAtLimit)
    }

    @Test
    fun `given one word turns and several words or a blank or an over-long word when validated then it is rejected`() {
        // When
        val twoWords = validator.validStoryText("a horse", oneWord)
        val splitByNewline = validator.validStoryText("a\nhorse", oneWord)
        val blank = validator.validStoryText(" ​ ", oneWord)
        val overLong = validator.validStoryText("a".repeat(MAX_WORD_LENGTH + 1), oneWord)

        // Then
        assertNull(twoWords)
        assertNull(splitByNewline)
        assertNull(blank)
        assertNull(overLong)
    }
}
