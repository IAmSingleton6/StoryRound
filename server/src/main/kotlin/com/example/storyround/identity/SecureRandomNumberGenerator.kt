package com.example.storyround.identity

import com.example.storyround.random.RandomNumberGenerator
import java.security.SecureRandom

internal class SecureRandomNumberGenerator : RandomNumberGenerator {
    private val secureRandom = SecureRandom()

    override fun nextInt(until: Int): Int = secureRandom.nextInt(until)
}
