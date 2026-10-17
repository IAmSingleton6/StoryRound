package com.example.storyround.game

import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.ErrorCode
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.room.RegistryResult
import com.example.storyround.room.RoomRegistry
import io.ktor.server.websocket.DefaultWebSocketServerSession

internal class ClientMessageHandlerImpl(
    private val registry: RoomRegistry,
    private val coordinator: RoomCoordinator,
    private val sockets: RoomSocketHub,
) : ClientMessageHandler {
    override suspend fun handle(
        session: DefaultWebSocketServerSession,
        message: ClientMessage,
    ) {
        when (message) {
            is ClientMessage.CreateRoom -> coordinator.runForNewRoom(session) { registry.createRoom(message.playerName) }
            is ClientMessage.JoinRoom ->
                runInRoom(session, message.roomCode) { roomCode -> registry.joinRoom(roomCode, message.playerName) }
            is ClientMessage.Reconnect ->
                runInRoom(session, message.roomCode) { roomCode ->
                    registry.reconnect(roomCode, message.playerId, message.sessionToken)
                }
            is ClientMessage.StartGame ->
                runAsPlayer(session, message.roomCode, message.playerId) { roomCode ->
                    registry.startGame(roomCode, message.playerId, message.openingPlayerId, message.settings)
                }
            is ClientMessage.LeaveRoom ->
                runAsPlayer(session, message.roomCode, message.playerId) { roomCode -> registry.leaveRoom(roomCode, message.playerId) }
            is ClientMessage.SubmitPrompt ->
                runAsPlayer(session, message.roomCode, message.playerId) { roomCode ->
                    registry.submitPrompt(roomCode, message.playerId, message.text)
                }
            is ClientMessage.SubmitContribution ->
                runAsPlayer(session, message.roomCode, message.playerId) { roomCode ->
                    registry.submitContribution(roomCode, message.playerId, message.text)
                }
            is ClientMessage.PlayAgain ->
                runAsPlayer(session, message.roomCode, message.playerId) { roomCode -> registry.playAgain(roomCode, message.playerId) }
        }
    }

    private suspend fun runInRoom(
        session: DefaultWebSocketServerSession,
        typedRoomCode: String,
        action: (roomCode: String) -> RegistryResult,
    ) {
        val roomCode = canonicalRoomCode(typedRoomCode)
        coordinator.runInRoom(roomCode, session) { action(roomCode) }
    }

    private suspend fun runAsPlayer(
        session: DefaultWebSocketServerSession,
        typedRoomCode: String,
        playerId: String,
        action: (roomCode: String) -> RegistryResult,
    ) {
        val roomCode = canonicalRoomCode(typedRoomCode)
        if (sockets.bindingOf(session) != PlayerBinding(roomCode, playerId)) {
            sockets.send(session, ServerMessage.Error(ErrorCode.NOT_CONNECTED_AS_PLAYER, "This connection is not playing as that player."))
            return
        }
        coordinator.runInRoom(roomCode, session) { action(roomCode) }
    }

    private fun canonicalRoomCode(typedRoomCode: String): String = typedRoomCode.trim().uppercase()
}
