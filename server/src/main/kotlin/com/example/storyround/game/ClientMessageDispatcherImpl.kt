package com.example.storyround.game

import com.example.storyround.protocol.ClientMessage
import com.example.storyround.room.RegistryResult
import com.example.storyround.room.RoomRegistry

internal class ClientMessageDispatcherImpl(
    private val registry: RoomRegistry,
) : ClientMessageDispatcher {
    override fun dispatch(message: ClientMessage): RegistryResult =
        when (message) {
            is ClientMessage.CreateRoom -> registry.createRoom(message.playerName)
            is ClientMessage.JoinRoom -> registry.joinRoom(message.roomCode, message.playerName)
            is ClientMessage.StartGame ->
                registry.startGame(message.roomCode, message.playerId, message.openingPlayerId, message.settings)
            is ClientMessage.LeaveRoom -> registry.leaveRoom(message.roomCode, message.playerId)
            is ClientMessage.SubmitPrompt -> registry.submitPrompt(message.roomCode, message.playerId, message.text)
            is ClientMessage.SubmitContribution -> registry.submitContribution(message.roomCode, message.playerId, message.text)
            is ClientMessage.PlayAgain -> registry.playAgain(message.roomCode, message.playerId)
            is ClientMessage.Reconnect -> registry.reconnect(message.roomCode, message.playerId, message.sessionToken)
        }
}
