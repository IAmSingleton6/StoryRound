package com.example.storyround.presentation.finished

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.usecase.PlayAgainUseCase
import com.example.storyround.presentation.mapper.RoomUiMapper
import com.example.storyround.presentation.model.StoryUi
import com.example.storyround.presentation.model.storySegments
import com.example.storyround.presentation.model.storyWriters
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class FinishedViewModel(
    repository: GameRepository,
    private val playAgainUseCase: PlayAgainUseCase,
    private val roomUiMapper: RoomUiMapper,
) : ViewModel() {
    val uiState: StateFlow<FinishedUiState> =
        repository.state
            .map { state ->
                val room = state.room?.let(roomUiMapper::map)
                val story = room?.let { StoryUi(it.roomCode, it.prompt, it.players, it.contributions, it.promptAuthor) }
                val segments = room?.let { storySegments(it.prompt, it.promptAuthor, it.contributions) }.orEmpty()
                FinishedUiState(
                    story = story,
                    segments = segments,
                    writers = room?.let { storyWriters(it.players, segments) }.orEmpty(),
                    host = room?.let { it.players.firstOrNull { player -> player.id == it.hostId } },
                    playerId = state.playerId,
                    canPlayAgain = room?.hostId == state.playerId,
                    isReconnecting = room != null && !state.isConnected,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinishedUiState())

    fun onAction(action: FinishedAction) {
        when (action) {
            FinishedAction.PlayAgain -> viewModelScope.launch { playAgainUseCase() }
            FinishedAction.Home -> Unit
        }
    }
}
