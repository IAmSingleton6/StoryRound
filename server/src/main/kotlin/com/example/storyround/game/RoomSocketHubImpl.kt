package com.example.storyround.game

import com.example.storyround.protocol.ServerMessage
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.serialization.json.Json

internal class RoomSocketHubImpl(
    private val json: Json,
) : RoomSocketHub {
    private val roomSessions = mutableMapOf<String, MutableSet<DefaultWebSocketServerSession>>()

    @Synchronized
    override fun subscribe(
        roomCode: String,
        session: DefaultWebSocketServerSession,
    ) {
        roomSessions.getOrPut(roomCode) { mutableSetOf() }.add(session)
    }

    @Synchronized
    override fun unsubscribe(session: DefaultWebSocketServerSession) {
        roomSessions.values.forEach { it.remove(session) }
        roomSessions.entries.removeAll { it.value.isEmpty() }
    }

    override suspend fun send(
        session: DefaultWebSocketServerSession,
        message: ServerMessage,
    ) {
        session.sendOrClose(encode(message))
    }

    override suspend fun broadcast(
        roomCode: String,
        message: ServerMessage,
    ) {
        val payload = encode(message)
        val sessions = synchronized(this) { roomSessions[roomCode]?.toList().orEmpty() }
        sessions.forEach { session -> session.sendOrClose(payload) }
    }

    private fun encode(message: ServerMessage): String = json.encodeToString(ServerMessage.serializer(), message)

    private suspend fun DefaultWebSocketServerSession.sendOrClose(payload: String) {
        runCatching { send(Frame.Text(payload)) }.onFailure { close() }
    }
}
