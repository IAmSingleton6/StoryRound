package com.example.storyround.presentation.model

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal sealed interface UiText {
    data class Resource(
        val resource: StringResource,
    ) : UiText

    data class Plain(
        val text: String,
    ) : UiText
}

@Composable
internal fun UiText.asString(): String =
    when (this) {
        is UiText.Resource -> stringResource(resource)
        is UiText.Plain -> text
    }
