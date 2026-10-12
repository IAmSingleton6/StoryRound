package com.example.storyround.rules

interface TurnRules {
    fun totalTurns(
        playerCount: Int,
        rounds: Int,
    ): Int

    fun isComplete(
        turnNumber: Int,
        playerCount: Int,
        rounds: Int,
    ): Boolean

    fun playerIndex(
        turnNumber: Int,
        playerCount: Int,
    ): Int

    fun round(
        turnNumber: Int,
        playerCount: Int,
    ): Int

    fun durationMillis(turnNumber: Int): Long
}
