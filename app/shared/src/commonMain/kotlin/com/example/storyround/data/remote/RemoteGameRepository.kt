package com.example.storyround.data.remote

import com.example.storyround.data.local.RemoteSessionStore
import com.example.storyround.data.local.StoryHistoryStore
import com.example.storyround.domain.model.GameState
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.logging.Logger
import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.RoomSettings
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.story.SavedStoryMapper
import com.example.storyround.time.TimeProvider
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
internal const val SERVER_UNREACHABLE_MESSAGE = "Can't reach Story Round right now. Check your internet connection and try again."
internal const val RECONNECTING_MESSAGE = "Reconnecting..."
internal const val UNREADABLE_MESSAGE = "Received an unreadable server message."
internal const val NO_ROOM_MESSAGE = "Join a room before taking this action."

internal class RemoteGameRepository(
    private val serverUrl: String,
    private val sessionStore: RemoteSessionStore,
    private val storyHistoryStore: StoryHistoryStore,
    private val reconnectPolicy: ReconnectPolicy,
    private val sessionReducer: GameSessionReducer,
    private val connectionManager: GameConnectionManager,
    private val savedStoryMapper: SavedStoryMapper,
    private val timeProvider: TimeProvider,
    private val scope: CoroutineScope,
    private val logger: Logger,
) : GameRepository {
    private val mutableState =
        MutableStateFlow(
            GameState(
                playerId = sessionStore.load()?.playerId,
                history = storyHistoryStore.load(),
            ),
        )
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

    override suspend fun loadHistory() {
        val history = storyHistoryStore.load()
        mutableState.update { it.copy(history = history, error = null) }
    }

    override suspend fun openStory(id: String): Boolean {
        val story = storyHistoryStore.load().firstOrNull { it.id == id } ?: return false
        mutableState.update { it.copy(selectedStory = story, error = null) }
        return true
    }

    override fun closeStory() {
        mutableState.update { it.copy(selectedStory = null) }
    }

    override fun disconnect() {
        reconnectJob?.cancel()
        reconnectJob = null
        connectionManager.disconnect()
        pendingPlayerName = null
        mutableState.update { GameState(history = it.history) }
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
                mutableState.update { it.copy(isConnecting = false, error = SERVER_UNREACHABLE_MESSAGE) }
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
            mutableState.update { it.copy(error = NO_ROOM_MESSAGE) }
            return
        }
        send(createMessage(room.roomCode, playerId))
    }

    private suspend fun send(message: ClientMessage) {
        if (!connectionManager.send(message)) {
            mutableState.update { it.copy(error = SERVER_UNREACHABLE_MESSAGE) }
        }
    }

    private fun handleConnection() {
        mutableState.update { it.copy(isConnecting = false, isConnected = true) }
    }

    private fun handleDisconnection(reason: String?) {
        mutableState.update { it.copy(isConnecting = false, isConnected = false, error = reason ?: it.error) }
        scheduleReconnect()
    }

    private suspend fun handleIncomingMessage(incomingMessage: GameSocketIncomingMessage) {
        when (incomingMessage) {
            is GameSocketIncomingMessage.Received -> handleServerMessage(incomingMessage.message)
            GameSocketIncomingMessage.Unreadable -> mutableState.update { it.copy(error = UNREADABLE_MESSAGE) }
        }
    }

    private fun handleServerMessage(message: ServerMessage) {
        val updatedHistory =
            (message as? ServerMessage.GameFinished)?.let { finished ->
                saveFinishedStory(finished.room)
                storyHistoryStore.load()
            }
        var applied: GameSessionReduction? = null
        mutableState.update { current ->
            val reduction =
                sessionReducer.reduce(
                    context = GameSessionContext(current, sessionToken, pendingPlayerName, connectionManager.isConnected),
                    message = message,
                )
            applied = reduction
            updatedHistory?.let { reduction.context.state.copy(history = it) } ?: reduction.context.state
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

    private fun saveFinishedStory(room: RoomState) {
        storyHistoryStore.save(savedStoryMapper.map(room, localStoryId(room), timeProvider.nowEpochMillis()))
    }

    private fun localStoryId(room: RoomState): String {
        val contributions = room.contributions.joinToString { "${it.playerId}:${it.turnNumber}:${it.text}" }
        return "local-${"${room.roomCode}|${room.prompt}|$contributions".hashCode()}"
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
                    mutableState.update { it.copy(isConnecting = true, error = RECONNECTING_MESSAGE) }
                    connect()
                }
            }
    }
}
