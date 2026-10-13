package com.example.storyround.presentation.model

import com.example.storyround.protocol.PlayerColour
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerUiTest {
    @Test
    fun `given a name when its initial is read then it is the first letter in capitals`() {
        assertEquals("A", PlayerUi("alex", "  alex", PlayerColour.Blue).initial)
    }

    @Test
    fun `given a blank name when its initial is read then a question mark is shown`() {
        assertEquals("?", PlayerUi("blank", "   ", PlayerColour.Blue).initial)
    }
}
