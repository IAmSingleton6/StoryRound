package com.example.storyround.game

import com.example.storyround.logging.Logger
import com.example.storyround.protocol.ServerMessage
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.serialization.json.Json

private const val LOG_TAG = "RoomSocketHub"

internal class RoomSocketHubImpl(
    private val json: Json,
    private val logger: Logger,
) : RoomSocketHub {
    private val bindings = mutableMapOf<DefaultWebSocketServerSession, PlayerBinding>()
    private val roomSessions = mutableMapOf<String, MutableSet<DefaultWebSocketServerSession>>()

    @Synchronized
    override fun bind(
        session: DefaultWebSocketServerSession,
        roomCode: String,
        playerId: String,
    ): PlayerBinding? {
        val previous = unbind(session)
        unbindPlayer(roomCode, playerId)
        bindings[session] = PlayerBinding(roomCode, playerId)
        roomSessions.getOrPut(roomCode) { mutableSetOf() }.add(session)
        return previous
    }

    @Synchronized
    override fun unbind(session: DefaultWebSocketServerSession): PlayerBinding? {
        val binding = bindings.remove(session) ?: return null
        roomSessions[binding.roomCode]?.let { sessions ->
            sessions.remove(session)
            if (sessions.isEmpty()) roomSessions.remove(binding.roomCode)
        }
        return binding
    }

    @Synchronized
    override fun unbindPlayer(
        roomCode: String,
        playerId: String,
    ) {
        roomSessions[roomCode]
            ?.filter { bindings[it]?.playerId == playerId }
            ?.forEach { unbind(it) }
    }

    @Synchronized
    override fun unbindRoom(roomCode: String) {
        roomSessions.remove(roomCode)?.forEach { bindings.remove(it) }
    }

    @Synchronized
    override fun bindingOf(session: DefaultWebSocketServerSession): PlayerBinding? = bindings[session]

    @Synchronized
    override fun hasConnections(roomCode: String): Boolean = roomSessions.containsKey(roomCode)

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
        runCatching { send(Frame.Text(payload)) }.onFailure { failure ->
            val player = bindingOf(this)?.let { "player ${it.playerId} in room ${it.roomCode}" } ?: "an unseated connection"
            logger.warn(LOG_TAG, "Closing the connection to $player after a failed send.", failure)
            close()
        }
    }
}
