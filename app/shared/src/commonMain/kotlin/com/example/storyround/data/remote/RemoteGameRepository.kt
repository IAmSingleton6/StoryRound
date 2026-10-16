package com.example.storyround.data.remote

import com.example.storyround.data.local.RemoteSessionStore
import com.example.storyround.domain.model.GameError
import com.example.storyround.domain.model.GameState
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.domain.repository.StoryHistoryRepository
import com.example.storyround.logging.Logger
import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.RoomSettings
import com.example.storyround.protocol.ServerMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile

private const val LOG_TAG = "RemoteGameRepository"

internal class RemoteGameRepository(
    private val serverUrl: String,
    private val sessionStore: RemoteSessionStore,
    private val storyHistoryRepository: StoryHistoryRepository,
    private val reconnectPolicy: ReconnectPolicy,
    private val sessionReducer: GameSessionReducer,
    private val connectionManager: GameConnectionManager,
    private val scope: CoroutineScope,
    private val logger: Logger,
) : GameRepository {
    private val mutableState = MutableStateFlow(GameState(playerId = sessionStore.load()?.playerId))
    private var reconnectJob: Job? = null

    @Volatile
    private var pendingPlayerName: String? = null

    @Volatile
    private var sessionToken: String? = sessionStore.load()?.sessionToken

    override val state: StateFlow<GameState> = mutableState.asStateFlow()

    override suspend fun connect() = openConnection(resumeSavedSession = true)

    override suspend fun createRoom(playerName: String) = sendWithPlayerName(playerName, ClientMessage.CreateRoom(playerName))

    override suspend fun joinRoom(
        roomCode: String,
        playerName: String,
    ) = sendWithPlayerName(playerName, ClientMessage.JoinRoom(roomCode, playerName))

    override suspend fun startGame(
        openingPlayerId: String?,
        settings: RoomSettings,
    ) = sendForCurrentPlayer { roomCode, playerId -> ClientMessage.StartGame(roomCode, playerId, openingPlayerId, settings) }

    override suspend fun submitPrompt(text: String) =
        sendForCurrentPlayer { roomCode, playerId -> ClientMessage.SubmitPrompt(roomCode, playerId, text) }

    override suspend fun submitContribution(text: String) =
        sendForCurrentPlayer { roomCode, playerId -> ClientMessage.SubmitContribution(roomCode, playerId, text) }

    override suspend fun playAgain() = sendForCurrentPlayer { roomCode, playerId -> ClientMessage.PlayAgain(roomCode, playerId) }

    override fun disconnect() {
        reconnectJob?.cancel()
        reconnectJob = null
        connectionManager.disconnect()
        pendingPlayerName = null
        mutableState.value = GameState()
    }

    override suspend fun leaveRoom() {
        val current = mutableState.value
        val room = current.room
        val playerId = current.playerId
        if (room != null && playerId != null && connectionManager.isConnected) {
            connectionManager.send(ClientMessage.LeaveRoom(room.roomCode, playerId))
        }
        disconnect()
        sessionStore.clear()
    }

    private suspend fun openConnection(resumeSavedSession: Boolean) {
        if (connectionManager.isConnected) return
        val savedSession = sessionStore.load()
        mutableState.update { it.copy(playerId = savedSession?.playerId, isConnecting = true, error = null) }
        val result =
            connectionManager.connect(
                serverUrl = serverUrl,
                onConnected = ::handleConnection,
                onIncomingMessage = ::handleIncomingMessage,
                onDisconnected = ::handleDisconnection,
            )
        when (result) {
            GameConnectionResult.Connected ->
                savedSession?.takeIf { resumeSavedSession }?.let { saved ->
                    send(ClientMessage.Reconnect(saved.roomCode, saved.playerId, saved.sessionToken))
                }
            is GameConnectionResult.Failed -> {
                logger.warn(LOG_TAG, "Could not connect to $serverUrl: ${result.message}")
                mutableState.update { it.copy(isConnecting = false, error = GameError.ServerUnreachable) }
            }
        }
    }

    private suspend fun sendWithPlayerName(
        playerName: String,
        message: ClientMessage,
    ) {
        pendingPlayerName = playerName.trim()
        openConnection(resumeSavedSession = false)
        send(message)
    }

    private suspend fun sendForCurrentPlayer(createMessage: (roomCode: String, playerId: String) -> ClientMessage) {
        val current = mutableState.value
        val room = current.room
        val playerId = current.playerId
        if (room == null || playerId == null) {
            mutableState.update { it.copy(error = GameError.NoRoom) }
            return
        }
        send(createMessage(room.roomCode, playerId))
    }

    private suspend fun send(message: ClientMessage) {
        if (!connectionManager.send(message)) {
            mutableState.update { it.copy(error = GameError.ServerUnreachable) }
        }
    }

    private fun handleConnection() {
        mutableState.update { it.copy(isConnecting = false, isConnected = true) }
    }

    private fun handleDisconnection(reason: String?) {
        mutableState.update { it.copy(isConnecting = false, isConnected = false, error = reason?.let(GameError::Server) ?: it.error) }
        scheduleReconnect()
    }

    private suspend fun handleIncomingMessage(incomingMessage: GameSocketIncomingMessage) {
        when (incomingMessage) {
            is GameSocketIncomingMessage.Received -> handleServerMessage(incomingMessage.message)
            GameSocketIncomingMessage.Unreadable -> mutableState.update { it.copy(error = GameError.UnreadableMessage) }
        }
    }

    private fun handleServerMessage(message: ServerMessage) {
        if (message is ServerMessage.GameFinished) storyHistoryRepository.saveFinished(message.room)
        var applied: GameSessionReduction? = null
        mutableState.update { current ->
            val reduction =
                sessionReducer.reduce(
                    context = GameSessionContext(current, sessionToken, pendingPlayerName, connectionManager.isConnected),
                    message = message,
                )
            applied = reduction
            reduction.context.state
        }
        val reduction = requireNotNull(applied)
        sessionToken = reduction.context.sessionToken
        pendingPlayerName = reduction.context.pendingPlayerName
        when (val command = reduction.persistenceCommand) {
            is SessionPersistenceCommand.Save -> sessionStore.save(command.session)
            SessionPersistenceCommand.Clear -> sessionStore.clear()
            null -> Unit
        }
    }

    private fun scheduleReconnect() {
        val shouldReconnect =
            reconnectPolicy.shouldReconnect(
                hasSavedSession = sessionStore.load() != null,
                reconnectInProgress = reconnectJob?.isActive == true,
            )
        if (!shouldReconnect) return
        reconnectJob =
            scope.launch {
                var attempt = 0
                while (!connectionManager.isConnected && sessionStore.load() != null) {
                    delay(reconnectPolicy.delayMillis(attempt++))
                    logger.info(LOG_TAG, "Reconnecting to $serverUrl (attempt $attempt).")
                    mutableState.update { it.copy(isConnecting = true, error = GameError.Reconnecting) }
                    connect()
                }
            }
    }
}
