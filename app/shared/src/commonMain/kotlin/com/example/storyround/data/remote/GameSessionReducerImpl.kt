package com.example.storyround.data.remote

import com.example.storyround.data.local.RemoteSession
import com.example.storyround.domain.model.GameState
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.ServerMessage

internal class GameSessionReducerImpl : GameSessionReducer {
    override fun reduce(
        context: GameSessionContext,
        message: ServerMessage,
    ): GameSessionReduction =
        when (message) {
            is ServerMessage.RoomStateChanged -> reduceRoomStateChanged(context, message.room)
            is ServerMessage.PlayerJoined -> reducePlayerJoined(context, message.player)
            is ServerMessage.SessionEstablished -> reduceSessionEstablished(context, message.playerId, message.sessionToken)
            is ServerMessage.Error -> reduceError(context, message)
            else -> GameSessionReduction(context)
        }

    private fun reduceRoomStateChanged(
        context: GameSessionContext,
        room: RoomState,
    ): GameSessionReduction {
        val updatedContext = context.copy(state = context.state.copy(room = room, error = null))
        return GameSessionReduction(updatedContext, updatedContext.persistSession(room))
    }

    private fun reducePlayerJoined(
        context: GameSessionContext,
        player: Player,
    ): GameSessionReduction {
        if (context.pendingPlayerName != player.name || context.state.playerId != null) {
            return GameSessionReduction(context)
        }
        return GameSessionReduction(
            context.copy(
                state = context.state.copy(playerId = player.id),
                pendingPlayerName = null,
            ),
        )
    }

    private fun reduceSessionEstablished(
        context: GameSessionContext,
        playerId: String,
        sessionToken: String,
    ): GameSessionReduction {
        val updatedContext =
            context.copy(
                state = context.state.copy(playerId = playerId),
                sessionToken = sessionToken,
                pendingPlayerName = null,
            )
        return GameSessionReduction(
            context = updatedContext,
            persistenceCommand = updatedContext.state.room?.let { room -> updatedContext.persistSession(room) },
        )
    }

    private fun reduceError(
        context: GameSessionContext,
        message: ServerMessage.Error,
    ): GameSessionReduction {
        if (message.code !in SESSION_FAILURE_CODES) {
            return GameSessionReduction(context.copy(state = context.state.copy(error = message.message)))
        }
        return GameSessionReduction(
            context =
                context.copy(
                    state =
                        GameState(
                            isConnected = context.isConnected,
                            history = context.state.history,
                            error = message.message.takeIf { context.state.room != null },
                        ),
                    sessionToken = null,
                    pendingPlayerName = null,
                ),
            persistenceCommand = SessionPersistenceCommand.Clear,
        )
    }

    private fun GameSessionContext.persistSession(room: RoomState): SessionPersistenceCommand.Save? {
        val playerId = state.playerId ?: return null
        val token = sessionToken ?: return null
        if (room.players.none { it.id == playerId }) return null
        return SessionPersistenceCommand.Save(RemoteSession(room.roomCode, playerId, token))
    }

    private companion object {
        val SESSION_FAILURE_CODES = setOf("ROOM_NOT_FOUND", "PLAYER_NOT_FOUND", "INVALID_SESSION")
    }
}
