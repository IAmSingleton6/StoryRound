package com.example.storyround.protocol

class UserTextValidatorImpl(
    private val sanitizer: UserTextSanitizer,
) : UserTextValidator {
    override fun validPlayerName(raw: String): String? =
        sanitizer.sanitize(raw).takeIf { it.length in MIN_PLAYER_NAME_LENGTH..MAX_PLAYER_NAME_LENGTH }

    override fun validStoryText(
        raw: String,
        settings: RoomSettings,
    ): String? =
        sanitizer.sanitize(raw).takeIf { text ->
            text.length in 1..settings.maxStoryTextLength && (!settings.oneWordPerTurn || text.isSingleWord())
        }
}

private fun String.isSingleWord(): Boolean = none(Char::isWhitespace)
