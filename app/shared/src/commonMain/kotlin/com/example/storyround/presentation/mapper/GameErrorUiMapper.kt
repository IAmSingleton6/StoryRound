package com.example.storyround.presentation.mapper

import com.example.storyround.domain.model.GameError
import com.example.storyround.presentation.model.UiText

internal interface GameErrorUiMapper {
    fun map(error: GameError): UiText
}
