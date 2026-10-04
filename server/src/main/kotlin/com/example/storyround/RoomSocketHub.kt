package com.example.storyround

import com.example.storyround.protocol.ServerMessage
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.serialization.json.Json

internal class RoomSocketHub(
    private val json: Json,
) {
    private val rooms = mutableMapOf<String, MutableSet<DefaultWebSocketServerSession>>()

    @Synchronized
    fun subscribe(
        roomCode: String,
        session: DefaultWebSocketServerSession,
    ) {
        rooms.getOrPut(roomCode) { mutableSetOf() }.add(session)
    }

    @Synchronized
    fun unsubscribe(session: DefaultWebSocketServerSession) {
        rooms.values.forEach { it.remove(session) }
        rooms.entries.removeAll { it.value.isEmpty() }
    }

    suspend fun send(
        session: DefaultWebSocketServerSession,
        message: ServerMessage,
    ) {
        val payload = json.encodeToString(ServerMessage.serializer(), message)
        runCatching { session.send(Frame.Text(payload)) }.onFailure { session.close() }
    }

    suspend fun broadcast(
        roomCode: String,
        message: ServerMessage,
    ) {
        val payload = json.encodeToString(ServerMessage.serializer(), message)
        val sessions = synchronized(this) { rooms[roomCode]?.toList().orEmpty() }
        sessions.forEach { session ->
            runCatching { session.send(Frame.Text(payload)) }.onFailure { session.close() }
        }
    }
}
