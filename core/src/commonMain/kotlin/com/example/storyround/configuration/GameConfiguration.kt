package com.example.storyround.configuration

data class GameConfiguration(
    val minRounds: Int = 1,
    val maxRounds: Int = 10,
    val defaultRounds: Int = 3,
    val maxPlayers: Int = 10,
    val firstTurnMillis: Long = 15_000L,
    val turnReductionMillis: Long = 1_000L,
    val minimumTurnMillis: Long = 5_000L,
) {
    init {
        require(minRounds > 0) { "The minimum rounds must be positive." }
        require(maxRounds >= minRounds) { "The maximum rounds must not be below the minimum." }
        require(defaultRounds in minRounds..maxRounds) { "The default rounds must be within the allowed range." }
        require(maxPlayers > 0) { "The maximum players must be positive." }
        require(firstTurnMillis > 0) { "The first turn duration must be positive." }
        require(turnReductionMillis >= 0) { "The turn reduction must not be negative." }
        require(minimumTurnMillis > 0) { "The minimum turn duration must be positive." }
    }

    fun turnDurationMillis(turnNumber: Int): Long = (firstTurnMillis - turnNumber * turnReductionMillis).coerceAtLeast(minimumTurnMillis)
}
