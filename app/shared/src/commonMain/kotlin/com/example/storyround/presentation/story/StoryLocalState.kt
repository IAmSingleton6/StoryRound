package com.example.storyround.presentation.story

internal data class StoryLocalState(
    val nowEpochMillis: Long,
    val openingPromptDraft: String = "",
    val contributionDraft: String = "",
    val timeUpHandOverPlayerId: String? = null,
)
