package com.example.storyround.ui.theme

import com.example.storyround.protocol.PlayerColour
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlayerColoursTest {
    @Test
    fun `given every player colour when drawn then each has its own distinct colour`() {
        val colours = PlayerColour.entries.map { it.toColor() }

        assertEquals(colours.size, colours.toSet().size)
    }

    @Test
    fun `given every player colour when text is drawn on it then it uses ink or white from the palette`() {
        val allowed = setOf(MidnightPaperColors.ink, MidnightPaperColors.paperWhite)

        assertTrue(PlayerColour.entries.all { it.toContentColor() in allowed })
        assertEquals(MidnightPaperColors.ink, PlayerColour.Yellow.toContentColor())
        assertEquals(MidnightPaperColors.paperWhite, PlayerColour.Blue.toContentColor())
    }
}
