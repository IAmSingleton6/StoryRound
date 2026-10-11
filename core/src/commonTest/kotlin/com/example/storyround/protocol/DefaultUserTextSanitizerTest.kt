package com.example.storyround.protocol

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultUserTextSanitizerTest {
    private val sanitizer = DefaultUserTextSanitizer()

    @Test
    fun `given surrounding and repeated whitespace when sanitised then it is trimmed and collapsed to single spaces`() {
        assertEquals("A door opened.", sanitizer.sanitize("  A \t door\n\n opened.  "))
    }

    @Test
    fun `given control and invisible formatting characters when sanitised then they are removed`() {
        assertEquals("Alex", sanitizer.sanitize("A\u0000l​e‮x\u0007"))
    }

    @Test
    fun `given text made only of whitespace and invisible characters when sanitised then it is empty`() {
        assertEquals("", sanitizer.sanitize(" ​\n⁠\t "))
    }

    @Test
    fun `given emoji and accented text when sanitised then it is preserved`() {
        val text = "Zoë 👩‍🚀 waved"

        assertEquals(text, sanitizer.sanitize(text))
    }

    @Test
    fun `given an unpaired surrogate when sanitised then it is removed`() {
        assertEquals("ab", sanitizer.sanitize("a\uD83Db"))
    }
}
