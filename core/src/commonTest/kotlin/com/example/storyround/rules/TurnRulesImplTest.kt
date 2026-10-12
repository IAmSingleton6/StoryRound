package com.example.storyround.rules

import com.example.storyround.configuration.GameConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TurnRulesImplTest {
    private val rules =
        TurnRulesImpl(GameConfiguration(firstTurnMillis = 15_000L, turnReductionMillis = 1_000L, minimumTurnMillis = 5_000L))

    @Test
    fun `given three players and two rounds when totalled then everyone writes twice`() {
        assertEquals(6, rules.totalTurns(playerCount = 3, rounds = 2))
    }

    @Test
    fun `given the turn after the last one when checked then the game is complete`() {
        assertFalse(rules.isComplete(turnNumber = 5, playerCount = 3, rounds = 2))
        assertTrue(rules.isComplete(turnNumber = 6, playerCount = 3, rounds = 2))
    }

    @Test
    fun `given successive turns when the writer is chosen then players take turns in order`() {
        assertEquals(listOf(0, 1, 2, 0, 1), (0..4).map { rules.playerIndex(it, playerCount = 3) })
    }

    @Test
    fun `given successive turns when the round is read then it advances once everyone has written`() {
        assertEquals(listOf(1, 1, 1, 2, 2), (0..4).map { rules.round(it, playerCount = 3) })
    }

    @Test
    fun `given successive turns when their duration is read then it follows the configured countdown`() {
        assertEquals(listOf(15_000L, 14_000L, 5_000L), listOf(0, 1, 40).map(rules::durationMillis))
    }
}
