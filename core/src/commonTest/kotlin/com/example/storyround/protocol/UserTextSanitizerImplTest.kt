package com.example.storyround.protocol

import kotlin.test.Test
import kotlin.test.assertEquals

class UserTextSanitizerImplTest {
    private val sanitizer = UserTextSanitizerImpl()

    @Test
    fun `given surrounding and repeated whitespace when sanitised then it is trimmed and collapsed to single spaces`() {
        // When
        val sanitized = sanitizer.sanitize("  A \t door\n\n opened.  ")

        // Then
        assertEquals("A door opened.", sanitized)
    }

    @Test
    fun `given control and invisible formatting characters when sanitised then they are removed`() {
        // When
        val sanitized = sanitizer.sanitize("A\u0000l​e‮x\u0007")

        // Then
        assertEquals("Alex", sanitized)
    }

    @Test
    fun `given text made only of whitespace and invisible characters when sanitised then it is empty`() {
        // When
        val sanitized = sanitizer.sanitize(" ​\n⁠\t ")

        // Then
        assertEquals("", sanitized)
    }

    @Test
    fun `given emoji and accented text when sanitised then it is preserved`() {
        // Given
        val text = "Zoë 👩‍🚀 waved"

        // When
        val sanitized = sanitizer.sanitize(text)

        // Then
        assertEquals(text, sanitized)
    }

    @Test
    fun `given an unpaired surrogate when sanitised then it is removed`() {
        // When
        val sanitized = sanitizer.sanitize("a\uD83Db")

        // Then
        assertEquals("ab", sanitized)
    }
}
