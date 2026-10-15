package com.example.storyround.ui.theme

import com.example.storyround.protocol.PlayerColour
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlayerColoursTest {
    @Test
    fun `given every player colour when drawn then each has its own distinct colour`() {
        // When
        val colours = PlayerColour.entries.map { it.toColor() }

        // Then
        assertEquals(colours.size, colours.toSet().size)
    }

    @Test
    fun `given every player colour when text is drawn on it then it uses ink or white from the palette`() {
        // Given
        val allowed = setOf(DarkColors.ink, DarkColors.paperWhite)

        // When
        val contentColours = PlayerColour.entries.associateWith { it.toContentColor() }

        // Then
        assertTrue(contentColours.values.all { it in allowed })
        assertEquals(DarkColors.ink, contentColours.getValue(PlayerColour.Yellow))
        assertEquals(DarkColors.paperWhite, contentColours.getValue(PlayerColour.Blue))
    }
}
