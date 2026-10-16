package com.example.storyround.configuration

import com.example.storyround.protocol.PlayerColour
import kotlin.test.Test
import kotlin.test.assertIs

class GameConfigurationTest {
    @Test
    fun `given a minimum of zero rounds when configured then it is rejected`() {
        // When
        val configuration = runCatching { GameConfiguration(minRounds = 0, defaultRounds = 1) }

        // Then
        assertIs<IllegalArgumentException>(configuration.exceptionOrNull())
    }

    @Test
    fun `given a maximum below the minimum rounds when configured then it is rejected`() {
        // When
        val configuration = runCatching { GameConfiguration(minRounds = 4, maxRounds = 3, defaultRounds = 3) }

        // Then
        assertIs<IllegalArgumentException>(configuration.exceptionOrNull())
    }

    @Test
    fun `given a default round count outside the allowed range when configured then it is rejected`() {
        // When
        val configuration = runCatching { GameConfiguration(minRounds = 2, maxRounds = 4, defaultRounds = 5) }

        // Then
        assertIs<IllegalArgumentException>(configuration.exceptionOrNull())
    }

    @Test
    fun `given a maximum of zero players when configured then it is rejected`() {
        // When
        val configuration = runCatching { GameConfiguration(maxPlayers = 0) }

        // Then
        assertIs<IllegalArgumentException>(configuration.exceptionOrNull())
    }

    @Test
    fun `given more players than there are colours when configured then it is rejected`() {
        // When
        val configuration = runCatching { GameConfiguration(maxPlayers = PlayerColour.entries.size + 1) }

        // Then
        assertIs<IllegalArgumentException>(configuration.exceptionOrNull())
    }

    @Test
    fun `given a first turn with no time when configured then it is rejected`() {
        // When
        val configuration = runCatching { GameConfiguration(firstTurnMillis = 0L) }

        // Then
        assertIs<IllegalArgumentException>(configuration.exceptionOrNull())
    }

    @Test
    fun `given a negative turn reduction when configured then it is rejected`() {
        // When
        val configuration = runCatching { GameConfiguration(turnReductionMillis = -1L) }

        // Then
        assertIs<IllegalArgumentException>(configuration.exceptionOrNull())
    }

    @Test
    fun `given a minimum turn with no time when configured then it is rejected`() {
        // When
        val configuration = runCatching { GameConfiguration(minimumTurnMillis = 0L) }

        // Then
        assertIs<IllegalArgumentException>(configuration.exceptionOrNull())
    }
}
