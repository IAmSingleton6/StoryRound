package com.example.storyround

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform