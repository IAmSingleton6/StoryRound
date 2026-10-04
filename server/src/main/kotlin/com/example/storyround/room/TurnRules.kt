package com.example.storyround.room

internal class TurnRules(
    val rounds: Int = DEFAULT_ROUNDS,
    private val firstTurnMillis: Long = DEFAULT_FIRST_TURN_MILLIS,
    private val reductionMillis: Long = DEFAULT_REDUCTION_MILLIS,
    private val minimumTurnMillis: Long = DEFAULT_MINIMUM_TURN_MILLIS,
) {
    fun isComplete(
        nextTurnNumber: Int,
        playerCount: Int,
    ): Boolean = nextTurnNumber >= rounds * playerCount

    fun currentPlayerIndex(
        turnNumber: Int,
        playerCount: Int,
    ): Int = turnNumber % playerCount

    fun durationMillis(turnNumber: Int): Long = (firstTurnMillis - turnNumber * reductionMillis).coerceAtLeast(minimumTurnMillis)

    companion object {
        const val DEFAULT_ROUNDS = 3
        const val DEFAULT_FIRST_TURN_MILLIS = 15_000L
        const val DEFAULT_REDUCTION_MILLIS = 1_000L
        const val DEFAULT_MINIMUM_TURN_MILLIS = 5_000L
    }
}
