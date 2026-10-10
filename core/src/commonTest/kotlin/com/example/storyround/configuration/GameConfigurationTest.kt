package com.example.storyround.configuration

import kotlin.test.Test
import kotlin.test.assertEquals

class GameConfigurationTest {
    private val configuration = GameConfiguration(firstTurnMillis = 15_000L, turnReductionMillis = 1_000L, minimumTurnMillis = 5_000L)

    @Test
    fun `given successive turns when durations are read then each turn has a second less`() {
        assertEquals(listOf(15_000L, 14_000L, 13_000L), (0..2).map(configuration::turnDurationMillis))
    }

    @Test
    fun `given a late turn when its duration is read then it never drops below the minimum`() {
        assertEquals(5_000L, configuration.turnDurationMillis(40))
    }
}
