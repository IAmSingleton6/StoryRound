package com.example.storyround.configuration

import com.example.storyround.protocol.PlayerColour
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

    @Test
    fun `given more players than there are colours when configured then it is rejected`() {
        assertFailsWith<IllegalArgumentException> { GameConfiguration(maxPlayers = PlayerColour.entries.size + 1) }
    }

    @Test
    fun `given a default round count outside the allowed range when configured then it is rejected`() {
        assertFailsWith<IllegalArgumentException> { GameConfiguration(minRounds = 2, maxRounds = 4, defaultRounds = 5) }
    }
}
