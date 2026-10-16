package com.example.storyround.data.remote

import com.example.storyround.logging.Logger
import com.example.storyround.protocol.ClientMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.Volatile

private const val LOG_TAG = "GameConnectionManager"

internal class GameConnectionManagerImpl(
    private val gameSocket: GameSocket,
    private val scope: CoroutineScope,
    private val logger: Logger,
) : GameConnectionManager {
    @Volatile
    private var session: GameSocketConnection? = null

    @Volatile
    private var reader: Job? = null
    private val connectLock = Mutex()

    override val isConnected: Boolean
        get() = session != null

    override suspend fun connect(
        serverUrl: String,
        onConnected: () -> Unit,
        onIncomingMessage: suspend (GameSocketIncomingMessage) -> Unit,
        onDisconnected: (String?) -> Unit,
    ): GameConnectionResult =
        connectLock.withLock {
            if (session != null) return GameConnectionResult.Connected
            val connectedSession =
                try {
                    gameSocket.connect(serverUrl)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (failure: Exception) {
                    return GameConnectionResult.Failed(failure.message ?: "unknown error")
                }
            session = connectedSession
            logger.info(LOG_TAG, "Connected to $serverUrl.")
            onConnected()
            reader = scope.launch { readUntilClosed(connectedSession, serverUrl, onIncomingMessage, onDisconnected) }
            GameConnectionResult.Connected
        }

    private suspend fun readUntilClosed(
        connectedSession: GameSocketConnection,
        serverUrl: String,
        onIncomingMessage: suspend (GameSocketIncomingMessage) -> Unit,
        onDisconnected: (String?) -> Unit,
    ) {
        try {
            connectedSession.incomingMessages.collect(onIncomingMessage)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            logger.warn(LOG_TAG, "The connection to $serverUrl dropped.", failure)
        } finally {
            if (session === connectedSession) {
                session = null
                val reason = connectedSession.closeReason()
                logger.info(LOG_TAG, "Disconnected from $serverUrl: ${reason ?: "no reason given"}.")
                onDisconnected(reason)
            }
        }
    }

    override suspend fun send(message: ClientMessage): Boolean {
        val activeSession = session ?: return false
        return try {
            activeSession.send(message)
            true
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            logger.warn(LOG_TAG, "Could not send ${message::class.simpleName}.", failure)
            false
        }
    }

    override fun disconnect() {
        val activeSession = session
        session = null
        reader?.cancel()
        reader = null
        if (activeSession != null) {
            logger.info(LOG_TAG, "Disconnecting on request.")
            scope.launch { activeSession.close() }
        }
    }
}
