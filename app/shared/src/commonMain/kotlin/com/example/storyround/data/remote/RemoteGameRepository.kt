package com.example.storyround.data.remote

import com.example.storyround.data.local.RemoteSessionStore
import com.example.storyround.data.local.StoryHistoryStore
import com.example.storyround.domain.model.RemoteGameState
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.logging.logger
import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.SavedStory
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.time.TimeProvider
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val LOG_TAG = "RemoteGameRepository"
internal const val SERVER_UNREACHABLE_MESSAGE = "Can't reach Story Round right now. Check your internet connection and try again."

internal class RemoteGameRepository(
    private val serverUrl: String,
    private val client: HttpClient,
    private val sessionStore: RemoteSessionStore,
    private val storyHistoryStore: StoryHistoryStore,
    private val reconnectPolicy: ReconnectPolicy,
    private val sessionReducer: GameSessionReducer,
    private val connectionManager: GameConnectionManager,
    private val timeProvider: TimeProvider,
) : GameRepository {
    private val log = logger()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutableState =
        MutableStateFlow(
            RemoteGameState(
                playerId = sessionStore.load()?.playerId,
                history = storyHistoryStore.load(),
            ),
        )
    private var reconnectJob: Job? = null
    private var pendingPlayerName: String? = null
    private var sessionToken: String? = sessionStore.load()?.sessionToken

    override val state: StateFlow<RemoteGameState> = mutableState.asStateFlow()

    override suspend fun connect() = openConnection(resumeSavedSession = true)

    private suspend fun openConnection(resumeSavedSession: Boolean) {
        if (connectionManager.isConnected) return
        val savedSession = sessionStore.load()
        mutableState.value = mutableState.value.copy(playerId = savedSession?.playerId, isConnecting = true, error = null)
        when (
            val result =
                connectionManager.connect(
                    serverUrl = serverUrl,
                    onConnected = ::handleConnection,
                    onIncomingMessage = ::handleIncomingMessage,
                    onDisconnected = ::handleDisconnection,
                )
        ) {
            GameConnectionResult.Connected -> {
                savedSession?.takeIf { resumeSavedSession }?.let { saved ->
                    send(ClientMessage.Reconnect(saved.roomCode, saved.playerId, saved.sessionToken))
                }
            }

            is GameConnectionResult.Failed -> {
                log.w("Could not connect to $serverUrl: ${result.message}", throwable = null, tag = LOG_TAG)
                mutableState.value = mutableState.value.copy(isConnecting = false, error = SERVER_UNREACHABLE_MESSAGE)
            }
        }
    }

    override suspend fun createRoom(playerName: String) = sendWithPlayerName(playerName, ClientMessage.CreateRoom(playerName))

    override suspend fun joinRoom(
        roomCode: String,
        playerName: String,
    ) = sendWithPlayerName(playerName, ClientMessage.JoinRoom(roomCode, playerName))

    override suspend fun startGame(
        openingPlayerId: String?,
        maxRounds: Int,
    ) = sendForCurrentPlayer { roomCode, playerId -> ClientMessage.StartGame(roomCode, playerId, openingPlayerId, maxRounds) }

    override suspend fun submitPrompt(text: String) =
        sendForCurrentPlayer { roomCode, playerId -> ClientMessage.SubmitPrompt(roomCode, playerId, text) }

    override suspend fun submitContribution(text: String) =
        sendForCurrentPlayer { roomCode, playerId -> ClientMessage.SubmitContribution(roomCode, playerId, text) }

    override suspend fun playAgain() = sendForCurrentPlayer { roomCode, playerId -> ClientMessage.PlayAgain(roomCode, playerId) }

    override suspend fun loadHistory() {
        mutableState.value = mutableState.value.copy(history = storyHistoryStore.load(), isLoadingHistory = false, error = null)
    }

    override suspend fun openStory(id: String): Boolean {
        val story = storyHistoryStore.load().firstOrNull { it.id == id } ?: return false
        mutableState.value = mutableState.value.copy(selectedStory = story, error = null)
        return true
    }

    override fun closeStory() {
        mutableState.value = mutableState.value.copy(selectedStory = null)
    }

    override fun disconnect() {
        reconnectJob?.cancel()
        reconnectJob = null
        connectionManager.disconnect()
        pendingPlayerName = null
        mutableState.value = RemoteGameState(history = mutableState.value.history)
    }

    override suspend fun leaveRoom() {
        val room = mutableState.value.room
        val playerId = mutableState.value.playerId
        if (room != null && playerId != null && connectionManager.isConnected) {
            connectionManager.send(ClientMessage.LeaveRoom(room.roomCode, playerId))
        }
        disconnect()
        sessionStore.clear()
    }

    override fun close() {
        disconnect()
        connectionManager.close()
        client.close()
        scope.cancel()
    }

    private suspend fun sendWithPlayerName(
        playerName: String,
        message: ClientMessage,
    ) {
        pendingPlayerName = playerName.trim()
        openConnection(resumeSavedSession = false)
        send(message)
    }

    private suspend fun sendForCurrentPlayer(createMessage: (String, String) -> ClientMessage) {
        val room = mutableState.value.room
        val playerId = mutableState.value.playerId
        if (room == null || playerId == null) {
            mutableState.value = mutableState.value.copy(error = "Join a room before taking this action.")
            return
        }
        send(createMessage(room.roomCode, playerId))
    }

    private suspend fun send(message: ClientMessage) {
        if (!connectionManager.send(message)) {
            mutableState.value = mutableState.value.copy(error = SERVER_UNREACHABLE_MESSAGE)
        }
    }

    private suspend fun handleIncomingMessage(incomingMessage: GameSocketIncomingMessage) {
        when (incomingMessage) {
            is GameSocketIncomingMessage.Received -> handleServerMessage(incomingMessage.message)
            is GameSocketIncomingMessage.Unreadable -> {
                mutableState.value = mutableState.value.copy(error = "Received an unreadable server message.")
            }
        }
    }

    private fun handleConnection() {
        mutableState.value = mutableState.value.copy(isConnecting = false, isConnected = true)
    }

    private fun handleDisconnection(reason: String?) {
        mutableState.value =
            mutableState.value.copy(
                isConnecting = false,
                isConnected = false,
                error = reason ?: mutableState.value.error,
            )
        scheduleReconnect()
    }

    private fun handleServerMessage(message: ServerMessage) {
        val reduction =
            sessionReducer.reduce(
                context =
                    GameSessionContext(
                        state = mutableState.value,
                        sessionToken = sessionToken,
                        pendingPlayerName = pendingPlayerName,
                        isConnected = connectionManager.isConnected,
                    ),
                message = message,
            )
        var updatedState = reduction.context.state
        if (message is ServerMessage.GameFinished) {
            val story = message.room.toLocalSavedStory()
            storyHistoryStore.save(story)
            updatedState =
                updatedState.copy(
                    history = storyHistoryStore.load(),
                    finishedStoryId = story.id,
                )
        }
        mutableState.value = updatedState
        sessionToken = reduction.context.sessionToken
        pendingPlayerName = reduction.context.pendingPlayerName
        when (val command = reduction.persistenceCommand) {
            is SessionPersistenceCommand.Save -> sessionStore.save(command.session)
            is SessionPersistenceCommand.Clear -> sessionStore.clear()
            null -> Unit
        }
    }

    private fun RoomState.toLocalSavedStory(): SavedStory {
        val promptText = requireNotNull(prompt)
        val openingPlayer = openingPlayerId ?: hostId
        val fingerprint = "$roomCode|$promptText|${contributions.joinToString { "${it.playerId}:${it.turnNumber}:${it.text}" }}"
        return SavedStory(
            id = "local-${fingerprint.hashCode()}",
            prompt = promptText,
            promptPlayerId = openingPlayer,
            createdAtEpochMillis = timeProvider.nowEpochMillis(),
            players = players,
            contributions = contributions,
        )
    }

    private fun scheduleReconnect() {
        if (
            !reconnectPolicy.shouldReconnect(
                hasSavedSession = sessionStore.load() != null,
                reconnectInProgress = reconnectJob?.isActive == true,
            )
        ) {
            return
        }
        reconnectJob =
            scope.launch {
                var attempt = 0
                while (!connectionManager.isConnected && sessionStore.load() != null) {
                    delay(reconnectPolicy.delayMillis(attempt++))
                    mutableState.value = mutableState.value.copy(isConnecting = true, error = "Reconnecting...")
                    connect()
                }
            }
    }
}
