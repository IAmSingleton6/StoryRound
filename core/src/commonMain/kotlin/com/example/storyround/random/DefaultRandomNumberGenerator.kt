package com.example.storyround.random

import kotlin.random.Random

class DefaultRandomNumberGenerator : RandomNumberGenerator {
    override fun nextInt(until: Int): Int = Random.nextInt(until)
}
