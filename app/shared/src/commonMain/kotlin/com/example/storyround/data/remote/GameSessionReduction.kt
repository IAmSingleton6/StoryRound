package com.example.storyround.data.remote

internal data class GameSessionReduction(
    val context: GameSessionContext,
    val persistenceCommand: SessionPersistenceCommand? = null,
)
