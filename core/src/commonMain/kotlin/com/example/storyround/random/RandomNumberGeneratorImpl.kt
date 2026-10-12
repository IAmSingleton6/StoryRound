package com.example.storyround.random

import kotlin.random.Random

class RandomNumberGeneratorImpl : RandomNumberGenerator {
    override fun nextInt(until: Int): Int = Random.nextInt(until)
}
