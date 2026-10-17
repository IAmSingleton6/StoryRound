package com.example.storyround.room

internal sealed interface TurnOutcome {
    data class NextTurn(
        val playerId: String,
        val deadlineEpochMillis: Long,
    ) : TurnOutcome

    data object GameFinished : TurnOutcome
}
