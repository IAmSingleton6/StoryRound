package com.example.storyround.game

import com.example.storyround.logging.Logger
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.room.RegistryResult
import com.example.storyround.room.RoomEvent
import com.example.storyround.room.RoomRegistry
import com.example.storyround.room.TurnOutcome
import com.example.storyround.time.TimeProvider
import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

private const val LOG_TAG = "RoomCoordinator"

internal class RoomCoordinatorImpl(
    private val registry: RoomRegistry,
    private val sockets: RoomSocketHub,
    private val timers: RoomTimers,
    private val timeProvider: TimeProvider,
    private val idleRoomTimeoutMillis: Long,
    private val logger: Logger,
) : RoomCoordinator {
    private val roomLocks = ConcurrentHashMap<String, Mutex>()

    override suspend fun runForNewRoom(
        sender: DefaultWebSocketServerSession,
        action: () -> RegistryResult,
    ) {
        when (val result = action()) {
            is RegistryResult.Success -> {
                roomLocks[result.room.roomCode] = Mutex()
                withRoomLock(result.room.roomCode) { reactTo(result, sender) }
            }
            is RegistryResult.Error -> sendError(sender, result)
        }
    }

    override suspend fun runInRoom(
        roomCode: String,
        sender: DefaultWebSocketServerSession,
        action: () -> RegistryResult,
    ) {
        withRoomLock(roomCode) {
            when (val result = action()) {
                is RegistryResult.Success -> reactTo(result, sender)
                is RegistryResult.Error -> sendError(sender, result)
            }
        }
    }

    override suspend fun connectionClosed(session: DefaultWebSocketServerSession) {
        val binding = sockets.bindingOf(session) ?: return
        withRoomLock(binding.roomCode) {
            if (sockets.unbind(session) != null) {
                logger.info(LOG_TAG, "Player ${binding.playerId} disconnected from room ${binding.roomCode}.")
                scheduleCloseIfEmpty(binding.roomCode)
            }
        }
    }

    private suspend fun runTimerInRoom(
        roomCode: String,
        action: () -> RegistryResult.Success?,
    ) {
        withRoomLock(roomCode) { action()?.let { reactTo(it, sender = null) } }
    }

    private suspend fun withRoomLock(
        roomCode: String,
        block: suspend () -> Unit,
    ) {
        val lock = roomLocks[roomCode] ?: return block()
        lock.withLock { block() }
    }

    private suspend fun reactTo(
        result: RegistryResult.Success,
        sender: DefaultWebSocketServerSession?,
    ) {
        val room = result.room
        logEvent(room, result.event)
        when (val event = result.event) {
            is RoomEvent.RoomCreated -> {
                seatPlayer(requireNotNull(sender), room, event.host, event.sessionToken)
                broadcastState(room)
                sockets.broadcast(room.roomCode, ServerMessage.PlayerJoined(event.host))
            }
            is RoomEvent.PlayerJoined -> {
                seatPlayer(requireNotNull(sender), room, event.player, event.sessionToken)
                broadcastState(room)
                sockets.broadcast(room.roomCode, ServerMessage.PlayerJoined(event.player))
            }
            is RoomEvent.PlayerReconnected -> {
                val session = requireNotNull(sender)
                seatPlayer(session, room, event.player, event.sessionToken)
                sockets.send(session, ServerMessage.RoomStateChanged(room))
                if (room.status == GameStatus.Finished) sockets.send(session, ServerMessage.GameFinished(room))
            }
            is RoomEvent.PlayerLeft -> {
                sockets.unbindPlayer(room.roomCode, event.playerId)
                scheduleCloseIfEmpty(room.roomCode)
                broadcastState(room)
                sockets.broadcast(room.roomCode, ServerMessage.PlayerLeft(event.playerId))
            }
            RoomEvent.GameStarted -> broadcastState(room)
            is RoomEvent.PromptSubmitted -> {
                broadcastState(room)
                startNextTurnOrFinish(room, event.firstTurn)
            }
            is RoomEvent.ContributionAdded -> {
                broadcastState(room)
                sockets.broadcast(room.roomCode, ServerMessage.ContributionAdded(event.contribution))
                startNextTurnOrFinish(room, event.outcome)
            }
            is RoomEvent.TurnExpired -> {
                broadcastState(room)
                sockets.broadcast(room.roomCode, ServerMessage.TurnExpired(event.playerId))
                startNextTurnOrFinish(room, event.outcome)
            }
            RoomEvent.GameReset -> broadcastState(room)
            RoomEvent.RoomClosed -> {
                timers.cancelAll(room.roomCode)
                sockets.unbindRoom(room.roomCode)
                roomLocks.remove(room.roomCode)
            }
        }
    }

    private fun logEvent(
        room: RoomState,
        event: RoomEvent,
    ) {
        val roomCode = room.roomCode
        when (event) {
            is RoomEvent.RoomCreated -> logger.info(LOG_TAG, "Room $roomCode created by player ${event.host.id}.")
            is RoomEvent.PlayerJoined -> logger.info(LOG_TAG, "Player ${event.player.id} joined room $roomCode.")
            is RoomEvent.PlayerReconnected -> logger.info(LOG_TAG, "Player ${event.player.id} reconnected to room $roomCode.")
            is RoomEvent.PlayerLeft -> logger.info(LOG_TAG, "Player ${event.playerId} left room $roomCode.")
            RoomEvent.GameStarted -> logger.info(LOG_TAG, "Game started in room $roomCode with ${room.players.size} players.")
            is RoomEvent.PromptSubmitted -> logger.debug(LOG_TAG, "Opening prompt submitted in room $roomCode.")
            is RoomEvent.ContributionAdded ->
                logger.debug(LOG_TAG, "Player ${event.contribution.playerId} took turn ${event.contribution.turnNumber} in room $roomCode.")
            is RoomEvent.TurnExpired -> logger.debug(LOG_TAG, "Player ${event.playerId}'s turn expired in room $roomCode.")
            RoomEvent.GameReset -> logger.info(LOG_TAG, "Room $roomCode reset for another game.")
            RoomEvent.RoomClosed -> logger.info(LOG_TAG, "Room $roomCode closed.")
        }
    }

    private suspend fun startNextTurnOrFinish(
        room: RoomState,
        outcome: TurnOutcome,
    ) {
        when (outcome) {
            is TurnOutcome.NextTurn -> {
                scheduleTurnExpiry(room.roomCode, outcome.deadlineEpochMillis)
                sockets.broadcast(room.roomCode, ServerMessage.TurnStarted(outcome.playerId, outcome.deadlineEpochMillis))
            }
            TurnOutcome.GameFinished -> {
                logger.info(LOG_TAG, "Game finished in room ${room.roomCode}.")
                timers.cancel(room.roomCode, RoomTimer.TurnExpiry)
                sockets.broadcast(room.roomCode, ServerMessage.GameFinished(room))
            }
        }
    }

    private suspend fun seatPlayer(
        session: DefaultWebSocketServerSession,
        room: RoomState,
        player: Player,
        sessionToken: String,
    ) {
        val previous = sockets.bind(session, room.roomCode, player.id)
        timers.cancel(room.roomCode, RoomTimer.IdleClose)
        previous?.takeIf { it.roomCode != room.roomCode }?.let { scheduleCloseIfEmpty(it.roomCode) }
        sockets.send(session, ServerMessage.SessionEstablished(player.id, sessionToken))
    }

    private fun scheduleTurnExpiry(
        roomCode: String,
        deadlineEpochMillis: Long,
    ) {
        timers.schedule(roomCode, RoomTimer.TurnExpiry, deadlineEpochMillis - timeProvider.nowEpochMillis()) {
            runTimerInRoom(roomCode) { registry.expireTurn(roomCode, deadlineEpochMillis) }
        }
    }

    private fun scheduleCloseIfEmpty(roomCode: String) {
        if (sockets.hasConnections(roomCode)) return
        logger.info(LOG_TAG, "Room $roomCode has no connections; closing it after ${idleRoomTimeoutMillis}ms unless a player returns.")
        timers.schedule(roomCode, RoomTimer.IdleClose, idleRoomTimeoutMillis) {
            runTimerInRoom(roomCode) { if (sockets.hasConnections(roomCode)) null else registry.closeRoom(roomCode) }
        }
    }

    private suspend fun broadcastState(room: RoomState) {
        sockets.broadcast(room.roomCode, ServerMessage.RoomStateChanged(room))
    }

    private suspend fun sendError(
        session: DefaultWebSocketServerSession,
        error: RegistryResult.Error,
    ) {
        val sender = sockets.bindingOf(session)?.let { "player ${it.playerId} in room ${it.roomCode}" } ?: "an unseated connection"
        logger.info(LOG_TAG, "Rejected an action from $sender: ${error.code}.")
        sockets.send(session, ServerMessage.Error(error.code, error.message))
    }
}
