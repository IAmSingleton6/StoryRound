package com.example.storyround.identity

internal interface RandomNumberGenerator {
    fun nextInt(until: Int): Int
}
