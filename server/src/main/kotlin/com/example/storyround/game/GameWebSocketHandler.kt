package com.example.storyround.game

import com.example.storyround.logging.Logger
import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.ErrorCode
import com.example.storyround.protocol.ServerMessage
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private const val LOG_TAG = "GameWebSocketHandler"

internal class GameWebSocketHandler(
    private val messageHandler: ClientMessageHandler,
    private val coordinator: RoomCoordinator,
    private val sockets: RoomSocketHub,
    private val json: Json,
    private val logger: Logger,
) {
    suspend fun handle(session: DefaultWebSocketServerSession) {
        try {
            for (frame in session.incoming) {
                if (frame !is Frame.Text) continue
                val message =
                    runCatching { json.decodeFromString<ClientMessage>(frame.readText()) }
                        .onFailure { logger.warn(LOG_TAG, "Ignored an unreadable client message (${it::class.simpleName}).") }
                        .getOrNull()
                if (message == null) {
                    sockets.send(
                        session = session,
                        message = ServerMessage.Error(ErrorCode.INVALID_MESSAGE, "Could not read that message."),
                    )
                    continue
                }
                messageHandler.handle(session, message)
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            logger.error(LOG_TAG, "A connection failed while handling its messages.", failure)
            throw failure
        } finally {
            withContext(NonCancellable) { coordinator.connectionClosed(session) }
        }
    }
}
