package com.example.storyround.protocol

class UserTextValidatorImpl(
    private val sanitizer: UserTextSanitizer,
) : UserTextValidator {
    override fun validPlayerName(raw: String): String? =
        sanitizer.sanitize(raw).takeIf { it.length in MIN_PLAYER_NAME_LENGTH..MAX_PLAYER_NAME_LENGTH }

    override fun validStoryText(raw: String): String? = sanitizer.sanitize(raw).takeIf { it.length in 1..MAX_STORY_TEXT_LENGTH }
}
