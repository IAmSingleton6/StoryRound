package com.example.storyround.protocol

interface UserTextValidator {
    fun validPlayerName(raw: String): String?

    fun validStoryText(
        raw: String,
        settings: RoomSettings,
    ): String?
}
