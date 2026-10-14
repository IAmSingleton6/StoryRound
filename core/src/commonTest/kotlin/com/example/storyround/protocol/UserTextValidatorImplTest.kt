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
        assertEquals("Mary Jane", validator.validPlayerName("  Mary \n Jane "))
    }

    @Test
    fun `given a name too short after sanitising or too long when validated then it is rejected`() {
        assertNull(validator.validPlayerName(" A​ "))
        assertNull(validator.validPlayerName("A".repeat(MAX_PLAYER_NAME_LENGTH + 1)))
    }

    @Test
    fun `given story text within the limit when validated then the sanitised text is returned`() {
        assertEquals("A door opened.", validator.validStoryText(" A door\nopened. ", freeWriting))
        assertEquals(MAX_STORY_TEXT_LENGTH, validator.validStoryText("a".repeat(MAX_STORY_TEXT_LENGTH), freeWriting)?.length)
    }

    @Test
    fun `given blank or over-long story text when validated then it is rejected`() {
        assertNull(validator.validStoryText(" ​\n ", freeWriting))
        assertNull(validator.validStoryText("a".repeat(MAX_STORY_TEXT_LENGTH + 1), freeWriting))
    }

    @Test
    fun `given one word turns and a padded word with punctuation when validated then the sanitised word is returned`() {
        assertEquals("\"Hello,", validator.validStoryText("  \"Hello, \n", oneWord))
        assertEquals("mother-in-law's", validator.validStoryText("mother-in-law's", oneWord))
        assertEquals(MAX_WORD_LENGTH, validator.validStoryText("a".repeat(MAX_WORD_LENGTH), oneWord)?.length)
    }

    @Test
    fun `given one word turns and several words, a blank or an over-long word when validated then it is rejected`() {
        assertNull(validator.validStoryText("a horse", oneWord))
        assertNull(validator.validStoryText("a\nhorse", oneWord))
        assertNull(validator.validStoryText(" ​ ", oneWord))
        assertNull(validator.validStoryText("a".repeat(MAX_WORD_LENGTH + 1), oneWord))
    }
}
