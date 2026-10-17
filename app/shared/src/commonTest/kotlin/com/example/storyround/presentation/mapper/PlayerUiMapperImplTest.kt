package com.example.storyround.presentation.mapper

import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerUiMapperImplTest {
    @Test
    fun `should keep the player's identity and colour when a protocol player is mapped`() {
        // Given
        val player = Player(id = "alex", name = "Alex", colour = PlayerColour.Blue)

        // When
        val mapped = PlayerUiMapperImpl().map(player)

        // Then
        assertEquals(PlayerUi("alex", "Alex", PlayerColour.Blue), mapped)
    }
}
