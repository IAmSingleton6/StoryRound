package com.example.storyround.presentation.mapper

import com.example.storyround.domain.model.GameError
import com.example.storyround.presentation.model.UiText
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.error_reconnecting
import storyround.app.shared.generated.resources.error_server_unreachable
import kotlin.test.Test
import kotlin.test.assertEquals

class GameErrorUiMapperImplTest {
    private val mapper = GameErrorUiMapperImpl()

    @Test
    fun `given an app error when mapped then the player sees its wording`() {
        // When
        val unreachable = mapper.map(GameError.ServerUnreachable)
        val reconnecting = mapper.map(GameError.Reconnecting)

        // Then
        assertEquals(UiText.Resource(Res.string.error_server_unreachable), unreachable)
        assertEquals(UiText.Resource(Res.string.error_reconnecting), reconnecting)
    }

    @Test
    fun `given a server error when mapped then the server's own message is shown`() {
        // When
        val mapped = mapper.map(GameError.Server("No game found with that code. Check it and try again."))

        // Then
        assertEquals(UiText.Plain("No game found with that code. Check it and try again."), mapped)
    }
}
