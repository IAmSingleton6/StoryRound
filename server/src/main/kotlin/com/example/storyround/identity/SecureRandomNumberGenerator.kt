package com.example.storyround.identity

import java.security.SecureRandom

internal class SecureRandomNumberGenerator : RandomNumberGenerator {
    private val secureRandom = SecureRandom()

    override fun nextInt(until: Int): Int = secureRandom.nextInt(until)
}
