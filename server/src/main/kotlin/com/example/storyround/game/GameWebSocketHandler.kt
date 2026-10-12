package com.example.storyround.game

import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.room.RegistryResult
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.json.Json

internal class GameWebSocketHandler(
    private val dispatcher: ClientMessageDispatcher,
    private val sockets: RoomSocketHub,
    private val completedStories: CompletedStoryService,
    private val turnExpiryScheduler: TurnExpiryScheduler,
    private val json: Json,
) {
    suspend fun handle(session: DefaultWebSocketServerSession) {
        try {
            for (frame in session.incoming) {
                if (frame !is Frame.Text) continue
                val message = runCatching { json.decodeFromString<ClientMessage>(frame.readText()) }.getOrNull()
                if (message == null) {
                    sockets.send(
                        session = session,
                        message = ServerMessage.Error("INVALID_MESSAGE", "Could not read that message."),
                    )
                    continue
                }
                handleMessage(session, message)
            }
        } finally {
            sockets.unsubscribe(session)
        }
    }

    private suspend fun handleMessage(
        session: DefaultWebSocketServerSession,
        message: ClientMessage,
    ) {
        when (val result = dispatcher.dispatch(message)) {
            is RegistryResult.Success -> handleSuccess(session, message, result)
            is RegistryResult.Error -> sockets.send(
                session = session,
                message = ServerMessage.Error(result.code, result.message)
            )
        }
    }

    private suspend fun handleSuccess(
        session: DefaultWebSocketServerSession,
        message: ClientMessage,
        result: RegistryResult.Success,
    ) {
        if (message is ClientMessage.LeaveRoom) {
            leaveRoom(session, result)
            return
        }

        subscribe(session, result)
        establishSession(session, result)
        handlePlayAgain(message, result)

        saveAndPublish(result)

        if (message is ClientMessage.Reconnect && result.room.status == GameStatus.Finished) {
            sendFinishedGame(session, result)
        }

        scheduleTurnExpiry(result)
    }


    private suspend fun leaveRoom(
        session: DefaultWebSocketServerSession,
        result: RegistryResult.Success,
    ) {
        sockets.unsubscribe(session)
        publish(result, savedStoryId = null)
    }

    private fun subscribe(
        session: DefaultWebSocketServerSession,
        result: RegistryResult.Success,
    ) {
        sockets.subscribe(result.room.roomCode, session)
    }

    private suspend fun establishSession(
        session: DefaultWebSocketServerSession,
        result: RegistryResult.Success,
    ) {
        val token = result.sessionToken ?: return
        sockets.send(
            session = session,
            message = ServerMessage.SessionEstablished(
                playerId = requireNotNull(result.affectedPlayer).id,
                sessionToken = token,
            ),
        )
    }

    private fun handlePlayAgain(
        message: ClientMessage,
        result: RegistryResult.Success,
    ) {
        if (message is ClientMessage.PlayAgain) {
            completedStories.forget(result.room.roomCode)
        }
    }

    private suspend fun sendFinishedGame(
        session: DefaultWebSocketServerSession,
        result: RegistryResult.Success,
    ) {
        val roomCode = result.room.roomCode
        sockets.send(
            session = session,
            message = ServerMessage.GameFinished(
                room = result.room,
                storyId = completedStories.savedStoryId(roomCode),
            ),
        )
    }

    private suspend fun saveAndPublish(result: RegistryResult.Success) {
        val saveResult = completedStories.saveIfFinished(result)
        publish(result, savedStoryId = (saveResult as? StorySaveResult.Saved)?.storyId)
        if (saveResult is StorySaveResult.Failed) {
            sockets.broadcast(
                roomCode = result.room.roomCode,
                message = ServerMessage.Error("STORY_SAVE_FAILED", "The game finished, but its story could not be saved."),
            )
        }
    }

    private suspend fun publish(
        result: RegistryResult.Success,
        savedStoryId: String?,
    ) {
        val roomCode = result.room.roomCode
        sockets.broadcast(roomCode, ServerMessage.RoomStateChanged(result.room))
        result.affectedPlayer?.takeIf { result.isNewPlayer }?.let { player ->
            sockets.broadcast(roomCode, ServerMessage.PlayerJoined(player))
        }
        result.events.forEach { event ->
            val message = if (event is ServerMessage.GameFinished) event.copy(storyId = savedStoryId) else event
            sockets.broadcast(roomCode, message)
        }
    }

    private fun scheduleTurnExpiry(result: RegistryResult.Success) {
        turnExpiryScheduler.schedule(result.room) { expiry ->
            saveAndPublish(expiry)
            scheduleTurnExpiry(expiry)
        }
    }
}
