package com.example.storyround.game

import com.example.storyround.RoomSocketHub
import com.example.storyround.persistence.StoryRepository
import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.SavedStory
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.room.RegistryResult
import com.example.storyround.room.RoomRegistry
import com.example.storyround.time.TimeProvider
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import java.util.concurrent.ConcurrentHashMap

internal class GameWebSocketHandler(
    private val registry: RoomRegistry,
    private val sockets: RoomSocketHub,
    private val storyRepository: StoryRepository?,
    private val json: Json,
    private val relayScope: CoroutineScope,
    private val logger: Logger,
    private val timeProvider: TimeProvider,
) {
    private val finishedStoryIds = ConcurrentHashMap<String, String>()

    suspend fun handle(session: DefaultWebSocketServerSession) {
        try {
            for (frame in session.incoming) {
                if (frame !is Frame.Text) continue
                val message =
                    runCatching {
                        json.decodeFromString<ClientMessage>(frame.readText())
                    }.getOrNull()
                if (message == null) {
                    session.sendError("INVALID_MESSAGE", "Could not read that message.")
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
        when (val result = registry.handle(message)) {
            is RegistryResult.Error -> session.sendError(result.code, result.message)
            is RegistryResult.Success -> handleSuccess(session, message, result)
        }
    }

    private suspend fun handleSuccess(
        session: DefaultWebSocketServerSession,
        message: ClientMessage,
        result: RegistryResult.Success,
    ) {
        sockets.subscribe(result.room.roomCode, session)
        result.sessionToken?.let { token ->
            sockets.send(
                session,
                ServerMessage.SessionEstablished(requireNotNull(result.affectedPlayer).id, token),
            )
        }
        val saveResult = persistFinishedStory(result)
        saveResult.story?.let { story -> finishedStoryIds[result.room.roomCode] = story.id }
        if (message is ClientMessage.PlayAgain) finishedStoryIds.remove(result.room.roomCode)
        publishRoomUpdate(result, saveResult.story?.id)
        if (saveResult.failed) {
            sockets.broadcast(
                result.room.roomCode,
                ServerMessage.Error("STORY_SAVE_FAILED", "The game finished, but its story could not be saved."),
            )
        }
        if (message is ClientMessage.Reconnect && result.room.status == GameStatus.Finished) {
            sockets.send(
                session,
                ServerMessage.GameFinished(result.room, finishedStoryIds[result.room.roomCode]),
            )
        }
        scheduleTurnExpiry(result)
    }

    private suspend fun publishRoomUpdate(
        result: RegistryResult.Success,
        savedStoryId: String? = null,
    ) {
        sockets.broadcast(result.room.roomCode, ServerMessage.RoomStateChanged(result.room))
        result.affectedPlayer?.takeIf { result.isNewPlayer }?.let { player ->
            sockets.broadcast(result.room.roomCode, ServerMessage.PlayerJoined(player))
        }
        result.events.forEach { event ->
            val message = if (event is ServerMessage.GameFinished) event.copy(storyId = savedStoryId) else event
            sockets.broadcast(result.room.roomCode, message)
        }
    }

    private suspend fun persistFinishedStory(result: RegistryResult.Success): StorySaveResult {
        if (result.room.status != GameStatus.Finished || result.events.none { it is ServerMessage.GameFinished }) {
            return StorySaveResult()
        }
        val repository = storyRepository ?: return StorySaveResult()
        return runCatching {
            withContext(Dispatchers.IO) {
                repository.save(result.room, timeProvider.nowEpochMillis())
            }
        }.fold(
            onSuccess = { story -> StorySaveResult(story = story) },
            onFailure = { failure ->
                logger.error("Could not save finished story for room ${result.room.roomCode}.", failure)
                StorySaveResult(failed = true)
            },
        )
    }

    private fun scheduleTurnExpiry(result: RegistryResult.Success) {
        val deadline = result.room.turnDeadlineEpochMillis ?: return
        relayScope.launch {
            delay((deadline - timeProvider.nowEpochMillis()).coerceAtLeast(0))
            registry.expireTurn(result.room.roomCode, deadline)?.let { expiry ->
                val saveResult = persistFinishedStory(expiry)
                publishRoomUpdate(expiry, saveResult.story?.id)
                if (saveResult.failed) {
                    sockets.broadcast(
                        expiry.room.roomCode,
                        ServerMessage.Error("STORY_SAVE_FAILED", "The game finished, but its story could not be saved."),
                    )
                }
                scheduleTurnExpiry(expiry)
            }
        }
    }

    private suspend fun RoomRegistry.handle(message: ClientMessage): RegistryResult =
        when (message) {
            is ClientMessage.CreateRoom -> createRoom(message.playerName)
            is ClientMessage.JoinRoom -> joinRoom(message.roomCode, message.playerName)
            is ClientMessage.StartGame -> startGame(message.roomCode, message.playerId)
            is ClientMessage.SubmitPrompt -> submitPrompt(message.roomCode, message.playerId, message.text)
            is ClientMessage.SubmitContribution -> submitContribution(message.roomCode, message.playerId, message.text)
            is ClientMessage.PlayAgain -> playAgain(message.roomCode, message.playerId)
            is ClientMessage.Reconnect -> reconnect(message.roomCode, message.playerId, message.sessionToken)
        }

    private suspend fun DefaultWebSocketServerSession.sendError(
        code: String,
        message: String,
    ) {
        send(Frame.Text(json.encodeToString(ServerMessage.serializer(), ServerMessage.Error(code, message))))
    }

    private data class StorySaveResult(
        val story: SavedStory? = null,
        val failed: Boolean = false,
    )
}
