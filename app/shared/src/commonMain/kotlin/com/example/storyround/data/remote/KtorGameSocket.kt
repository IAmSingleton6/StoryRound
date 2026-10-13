package com.example.storyround.data.remote

import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.ServerMessage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

internal class KtorGameSocket(
    private val client: HttpClient,
    private val json: Json,
) : GameSocket {
    override suspend fun connect(serverUrl: String): GameSocketConnection =
        KtorGameSocketConnection(
            session = client.webSocketSession(urlString = serverUrl),
            json = json,
        )
}

private class KtorGameSocketConnection(
    private val session: DefaultClientWebSocketSession,
    private val json: Json,
) : GameSocketConnection {
    override val incomingMessages: Flow<GameSocketIncomingMessage> =
        flow {
            for (frame in session.incoming) {
                if (frame !is Frame.Text) continue
                val message = runCatching { json.decodeFromString<ServerMessage>(frame.readText()) }.getOrNull()
                emit(message?.let(GameSocketIncomingMessage::Received) ?: GameSocketIncomingMessage.Unreadable)
            }
        }

    override suspend fun send(message: ClientMessage) {
        session.send(Frame.Text(json.encodeToString(ClientMessage.serializer(), message)))
    }

    override suspend fun close() {
        session.close()
    }

    override suspend fun closeReason(): String? =
        session.closeReason
            .await()
            ?.message
            ?.takeIf(String::isNotBlank)
}
