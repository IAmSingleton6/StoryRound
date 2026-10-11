package com.example.storyround.protocol

interface UserTextSanitizer {
    fun sanitize(raw: String): String
}
