package com.example.storyround.data.remote

import com.example.storyround.protocol.ClientMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile

internal class GameConnectionManagerImpl(
    private val gameSocket: GameSocket,
    private val scope: CoroutineScope,
) : GameConnectionManager {
    @Volatile
    private var session: GameSocketConnection? = null
    private var reader: Job? = null

    override val isConnected: Boolean
        get() = session != null

    override suspend fun connect(
        serverUrl: String,
        onConnected: () -> Unit,
        onIncomingMessage: suspend (GameSocketIncomingMessage) -> Unit,
        onDisconnected: (String?) -> Unit,
    ): GameConnectionResult {
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
        return GameConnectionResult.Connected
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
}
