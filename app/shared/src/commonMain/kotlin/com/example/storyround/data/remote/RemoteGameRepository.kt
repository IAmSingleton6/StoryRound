package com.example.storyround.data.remote

import com.example.storyround.data.local.RemoteSessionStore
import com.example.storyround.data.local.SettingsRemoteSessionStore
import com.example.storyround.domain.model.RemoteGameState
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.protocol.StorySummary
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
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

internal class RemoteGameRepository(
    private val serverUrl: String,
    private val client: HttpClient = HttpClient { install(WebSockets) },
    private val sessionStore: RemoteSessionStore = SettingsRemoteSessionStore(Settings()),
    private val reconnectPolicy: ReconnectPolicy = FixedIntervalReconnectPolicy(),
    private val sessionReducer: GameSessionReducer = GameSessionReducer(),
    private val gameSocket: GameSocket = KtorGameSocket(client),
    private val storyHistoryDataSource: StoryHistoryDataSource = KtorStoryHistoryDataSource(client),
    private val connectionManager: GameConnectionManager = DefaultGameConnectionManager(gameSocket),
) : GameRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutableState = MutableStateFlow(RemoteGameState(playerId = sessionStore.load()?.playerId))
    private var reconnectJob: Job? = null
    private var pendingPlayerName: String? = null
    private var sessionToken: String? = sessionStore.load()?.sessionToken

    override val state: StateFlow<RemoteGameState> = mutableState.asStateFlow()

    override suspend fun connect() {
        if (connectionManager.isConnected) return
        val savedSession = sessionStore.load()
        mutableState.value = mutableState.value.copy(playerId = savedSession?.playerId, isConnecting = true, error = null)
        when (
            val result =
                connectionManager.connect(
                    serverUrl = serverUrl,
                    onIncomingMessage = ::handleIncomingMessage,
                    onDisconnected = ::handleDisconnection,
                )
        ) {
            GameConnectionResult.Connected -> {
                mutableState.value = mutableState.value.copy(isConnecting = false, isConnected = true)
                savedSession?.let { saved ->
                    send(ClientMessage.Reconnect(saved.roomCode, saved.playerId, saved.sessionToken))
                }
            }

            is GameConnectionResult.Failed -> {
                mutableState.value =
                    mutableState.value.copy(
                        isConnecting = false,
                        error = "Could not connect to the game server: ${result.message}",
                    )
            }
        }
    }

    override suspend fun createRoom(playerName: String) = sendWithPlayerName(playerName, ClientMessage.CreateRoom(playerName))

    override suspend fun joinRoom(
        roomCode: String,
        playerName: String,
    ) = sendWithPlayerName(playerName, ClientMessage.JoinRoom(roomCode, playerName))

    override suspend fun startGame() = sendForCurrentPlayer { roomCode, playerId -> ClientMessage.StartGame(roomCode, playerId) }

    override suspend fun submitPrompt(text: String) =
        sendForCurrentPlayer { roomCode, playerId -> ClientMessage.SubmitPrompt(roomCode, playerId, text) }

    override suspend fun submitContribution(text: String) =
        sendForCurrentPlayer { roomCode, playerId -> ClientMessage.SubmitContribution(roomCode, playerId, text) }

    override suspend fun playAgain() = sendForCurrentPlayer { roomCode, playerId -> ClientMessage.PlayAgain(roomCode, playerId) }

    override suspend fun loadHistory() = loadHistoryResult { storyHistoryDataSource.loadStories(serverUrl) }

    override suspend fun openStory(id: String): Boolean {
        mutableState.value = mutableState.value.copy(isLoadingHistory = true, error = null)
        return runCatching { storyHistoryDataSource.loadStory(serverUrl, id) }.fold(
            onSuccess = { story ->
                mutableState.value = mutableState.value.copy(selectedStory = story, isLoadingHistory = false)
                true
            },
            onFailure = { failure ->
                mutableState.value =
                    mutableState.value.copy(
                        isLoadingHistory = false,
                        error = failure.message ?: "Could not load this story.",
                    )
                false
            },
        )
    }

    override fun closeStory() {
        mutableState.value = mutableState.value.copy(selectedStory = null)
    }

    override fun disconnect() {
        reconnectJob?.cancel()
        reconnectJob = null
        connectionManager.disconnect()
        pendingPlayerName = null
        mutableState.value = RemoteGameState()
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
            mutableState.value = mutableState.value.copy(error = "Connect to the game server first.")
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
        mutableState.value = reduction.context.state
        sessionToken = reduction.context.sessionToken
        pendingPlayerName = reduction.context.pendingPlayerName
        when (val command = reduction.persistenceCommand) {
            is SessionPersistenceCommand.Save -> sessionStore.save(command.session)
            is SessionPersistenceCommand.Clear -> sessionStore.clear()
            null -> Unit
        }
    }

    private suspend fun loadHistoryResult(load: suspend () -> List<StorySummary>) {
        mutableState.value = mutableState.value.copy(isLoadingHistory = true, error = null)
        runCatching { load() }
            .onSuccess { stories ->
                mutableState.value = mutableState.value.copy(history = stories, isLoadingHistory = false)
            }.onFailure { failure ->
                mutableState.value =
                    mutableState.value.copy(
                        isLoadingHistory = false,
                        error = failure.message ?: "Could not load story history.",
                    )
            }
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
