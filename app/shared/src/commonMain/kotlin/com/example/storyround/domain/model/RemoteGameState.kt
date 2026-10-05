package com.example.storyround.domain.model

import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.SavedStory
import com.example.storyround.protocol.StorySummary

internal data class RemoteGameState(
    val room: RoomState? = null,
    val playerId: String? = null,
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val history: List<StorySummary> = emptyList(),
    val selectedStory: SavedStory? = null,
    val finishedStoryId: String? = null,
    val isLoadingHistory: Boolean = false,
    val error: String? = null,
)
