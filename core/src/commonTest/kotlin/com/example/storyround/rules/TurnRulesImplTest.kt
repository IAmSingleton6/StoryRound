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
        // When
        val totalTurns = rules.totalTurns(playerCount = 3, rounds = 2)

        // Then
        assertEquals(6, totalTurns)
    }

    @Test
    fun `given the turn after the last one when checked then the game is complete`() {
        // When
        val lastTurnComplete = rules.isComplete(turnNumber = 5, playerCount = 3, rounds = 2)
        val turnAfterLastComplete = rules.isComplete(turnNumber = 6, playerCount = 3, rounds = 2)

        // Then
        assertFalse(lastTurnComplete)
        assertTrue(turnAfterLastComplete)
    }

    @Test
    fun `given successive turns when the writer is chosen then players take turns in order`() {
        // When
        val writers = (0..4).map { rules.playerIndex(it, playerCount = 3) }

        // Then
        assertEquals(listOf(0, 1, 2, 0, 1), writers)
    }

    @Test
    fun `given successive turns when the round is read then it advances once everyone has written`() {
        // When
        val rounds = (0..4).map { rules.round(it, playerCount = 3) }

        // Then
        assertEquals(listOf(1, 1, 1, 2, 2), rounds)
    }

    @Test
    fun `given successive turns when their duration is read then it follows the configured countdown`() {
        // When
        val durations = listOf(0, 1, 40).map(rules::durationMillis)

        // Then
        assertEquals(listOf(15_000L, 14_000L, 5_000L), durations)
    }
}
