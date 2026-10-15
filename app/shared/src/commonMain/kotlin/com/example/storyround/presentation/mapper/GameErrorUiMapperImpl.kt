package com.example.storyround.presentation.mapper

import com.example.storyround.domain.model.GameError
import com.example.storyround.presentation.model.UiText
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.error_no_room
import storyround.app.shared.generated.resources.error_reconnecting
import storyround.app.shared.generated.resources.error_server_unreachable
import storyround.app.shared.generated.resources.error_unreadable_message

internal class GameErrorUiMapperImpl : GameErrorUiMapper {
    override fun map(error: GameError): UiText =
        when (error) {
            GameError.ServerUnreachable -> UiText.Resource(Res.string.error_server_unreachable)
            GameError.Reconnecting -> UiText.Resource(Res.string.error_reconnecting)
            GameError.UnreadableMessage -> UiText.Resource(Res.string.error_unreadable_message)
            GameError.NoRoom -> UiText.Resource(Res.string.error_no_room)
            is GameError.Server -> UiText.Plain(error.message)
        }
}
