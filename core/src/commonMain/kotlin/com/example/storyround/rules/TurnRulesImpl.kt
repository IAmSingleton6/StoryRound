package com.example.storyround.rules

import com.example.storyround.configuration.GameConfiguration

class TurnRulesImpl(
    private val gameConfiguration: GameConfiguration,
) : TurnRules {
    override fun totalTurns(
        playerCount: Int,
        rounds: Int,
    ): Int = rounds * playerCount

    override fun isComplete(
        turnNumber: Int,
        playerCount: Int,
        rounds: Int,
    ): Boolean = turnNumber >= totalTurns(playerCount, rounds)

    override fun playerIndex(
        turnNumber: Int,
        playerCount: Int,
    ): Int = turnNumber % playerCount

    override fun round(
        turnNumber: Int,
        playerCount: Int,
    ): Int = turnNumber / playerCount + 1

    override fun durationMillis(turnNumber: Int): Long = gameConfiguration.turnDurationMillis(turnNumber)
}
