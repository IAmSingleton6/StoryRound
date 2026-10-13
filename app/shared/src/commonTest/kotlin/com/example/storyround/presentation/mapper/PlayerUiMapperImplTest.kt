package com.example.storyround.presentation.mapper

import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerUiMapperImplTest {
    @Test
    fun `given a protocol player when mapped then its id, name and colour are kept`() {
        val mapped = PlayerUiMapperImpl().map(Player(id = "alex", name = "Alex", colour = PlayerColour.Blue))

        assertEquals(PlayerUi("alex", "Alex", PlayerColour.Blue), mapped)
    }
}
