package com.example.storyround.presentation.model

import kotlin.test.Test
import kotlin.test.assertEquals

class PluralTest {
    @Test
    fun `given one when counted then the singular is used`() {
        assertEquals("1 writer", countOf(1, "writer"))
    }

    @Test
    fun `given zero or many when counted then the plural is used`() {
        assertEquals("0 writers", countOf(0, "writer"))
        assertEquals("3 saved stories", countOf(3, "saved story", "saved stories"))
    }
}
