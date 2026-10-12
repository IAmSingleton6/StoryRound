package com.example.storyround.protocol

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserTextValidatorImplTest {
    private val validator = UserTextValidatorImpl(UserTextSanitizerImpl())

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
        assertEquals("A door opened.", validator.validStoryText(" A door\nopened. "))
        assertEquals(MAX_STORY_TEXT_LENGTH, validator.validStoryText("a".repeat(MAX_STORY_TEXT_LENGTH))?.length)
    }

    @Test
    fun `given blank or over-long story text when validated then it is rejected`() {
        assertNull(validator.validStoryText(" ​\n "))
        assertNull(validator.validStoryText("a".repeat(MAX_STORY_TEXT_LENGTH + 1)))
    }
}
