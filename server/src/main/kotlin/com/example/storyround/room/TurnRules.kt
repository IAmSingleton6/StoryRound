package com.example.storyround.room

import com.example.storyround.configuration.GameConfiguration

internal class TurnRules(
    private val gameConfiguration: GameConfiguration,
) {
    fun isComplete(
        nextTurnNumber: Int,
        playerCount: Int,
        maxRounds: Int,
    ): Boolean = nextTurnNumber >= maxRounds * playerCount

    fun currentPlayerIndex(
        turnNumber: Int,
        playerCount: Int,
    ): Int = turnNumber % playerCount

    fun durationMillis(turnNumber: Int): Long =
        (gameConfiguration.firstTurnMillis - turnNumber * gameConfiguration.turnReductionMillis)
            .coerceAtLeast(gameConfiguration.minimumTurnMillis)
}
