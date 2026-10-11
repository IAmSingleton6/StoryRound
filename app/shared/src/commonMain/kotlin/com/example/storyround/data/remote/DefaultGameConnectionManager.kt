package com.example.storyround.data.remote

import com.example.storyround.protocol.ClientMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class DefaultGameConnectionManager(
    private val gameSocket: GameSocket,
) : GameConnectionManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var reader: Job? = null
    private var session: GameSocketConnection? = null

    override val isConnected: Boolean
        get() = session != null

    override suspend fun connect(
        serverUrl: String,
        onConnected: () -> Unit,
        onIncomingMessage: suspend (GameSocketIncomingMessage) -> Unit,
        onDisconnected: (String?) -> Unit,
    ): GameConnectionResult {
        if (session != null) return GameConnectionResult.Connected
        return runCatching { gameSocket.connect(serverUrl) }.fold(
            onSuccess = { connectedSession ->
                session = connectedSession
                onConnected()
                reader =
                    scope.launch {
                        try {
                            connectedSession.incomingMessages.collect(onIncomingMessage)
                        } catch (cancellation: CancellationException) {
                            throw cancellation
                        } catch (_: Exception) {
                        } finally {
                            if (session === connectedSession) {
                                session = null
                                onDisconnected(connectedSession.closeReason())
                            }
                        }
                    }
                GameConnectionResult.Connected
            },
            onFailure = { failure ->
                GameConnectionResult.Failed(failure.message ?: "unknown error")
            },
        )
    }

    override suspend fun send(message: ClientMessage): Boolean {
        val activeSession = session ?: return false
        return try {
            activeSession.send(message)
            true
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            false
        }
    }

    override fun disconnect() {
        val activeSession = session
        session = null
        reader?.cancel()
        reader = null
        if (activeSession != null) {
            scope.launch { activeSession.close() }
        }
    }

    override fun close() {
        disconnect()
        scope.cancel()
    }
}
