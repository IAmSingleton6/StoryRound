package com.example.storyround.testing.actor

import com.example.storyround.data.remote.GameSocket
import com.example.storyround.data.remote.GameSocketConnection
import com.example.storyround.data.remote.GameSocketIncomingMessage
import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.testing.awaitInRealTime
import com.example.storyround.testing.fixtures.tokenOf
import com.example.storyround.testing.withinRealTime
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class ScriptedServerRobot {
    private val connections = mutableListOf<ScriptedConnection>()
    private val sent = mutableListOf<ClientMessage>()

    @Volatile
    private var isRefusing = false

    internal val gameSocket: GameSocket =
        mockk {
            coEvery { connect(any()) } answers {
                if (isRefusing) throw IOException("Connection refused")
                ScriptedConnection().also { synchronized(connections) { connections += it } }.connection
            }
        }

    fun refuseConnections() {
        isRefusing = true
    }

    fun acceptConnections() {
        isRefusing = false
    }

    suspend fun sendMessage(message: ServerMessage) = openConnection().deliver(GameSocketIncomingMessage.Received(message))

    suspend fun sendUnreadableFrame() = openConnection().deliver(GameSocketIncomingMessage.Unreadable)

    suspend fun dropConnection(reason: String? = null) = openConnection().drop(reason)

    suspend fun admit(
        player: Player,
        room: RoomState,
    ) {
        sendMessage(ServerMessage.SessionEstablished(player.id, tokenOf(player)))
        sendMessage(ServerMessage.RoomStateChanged(room))
        sendMessage(ServerMessage.PlayerJoined(player))
    }

    suspend fun resume(
        player: Player,
        room: RoomState,
    ) {
        sendMessage(ServerMessage.SessionEstablished(player.id, tokenOf(player)))
        sendMessage(ServerMessage.RoomStateChanged(room))
        if (room.status == GameStatus.Finished) sendMessage(ServerMessage.GameFinished(room))
    }

    suspend fun sendRoom(room: RoomState) = sendMessage(ServerMessage.RoomStateChanged(room))

    suspend fun startTurn(room: RoomState) {
        sendRoom(room)
        sendMessage(ServerMessage.TurnStarted(requireNotNull(room.currentPlayerId), requireNotNull(room.turnDeadlineEpochMillis)))
    }

    suspend fun finishGame(room: RoomState) {
        sendRoom(room)
        sendMessage(ServerMessage.GameFinished(room))
    }

    suspend fun refuse(
        code: String,
        message: String,
    ) = sendMessage(ServerMessage.Error(code, message))

    suspend fun awaitSent(expected: ClientMessage) = takeSent(expected.toString()) { it == expected }

    suspend fun assertSent(expected: ClientMessage) = awaitSent(expected)

    suspend inline fun <reified T : ClientMessage> assertSentOfType(): T = takeSent(T::class.simpleName.orEmpty()) { it is T } as T

    suspend fun assertNothingSent(window: Duration = QUIET_WINDOW) {
        withinRealTime("the app to stay quiet") {
            delay(window)
        }
        val unread = synchronized(sent) { sent.toList() }
        check(unread.isEmpty()) { "The app sent: $unread" }
    }

    suspend fun awaitAppCaughtUp() {
        val connection = openConnection()
        connection.deliver(GameSocketIncomingMessage.Received(ServerMessage.PlayerJoined(CATCH_UP_MARKER)))
        awaitInRealTime("the app to read every message") { connection.isDrained }
    }

    suspend fun assertNoNewConnectionAttempts(window: Duration = QUIET_WINDOW) {
        val before = synchronized(connections) { connections.size }
        withinRealTime("the app to stop reconnecting") { delay(window) }
        val after = synchronized(connections) { connections.size }
        check(after == before) { "The app made ${after - before} more connection attempts." }
    }

    suspend fun assertConnectionAttempts(count: Int) =
        awaitInRealTime("the app to have connected $count times") { synchronized(connections) { connections.size } >= count }

    @PublishedApi
    internal suspend fun takeSent(
        description: String,
        matches: (ClientMessage) -> Boolean,
    ): ClientMessage =
        withinRealTime("the app to send $description", describeFailure = { "It sent: ${synchronized(sent) { sent.toList() }}" }) {
            var found: ClientMessage? = null
            while (found == null) {
                found = synchronized(sent) { sent.firstOrNull(matches)?.also { sent.remove(it) } }
                if (found == null) delay(POLL_INTERVAL)
            }
            found
        }

    private suspend fun openConnection(): ScriptedConnection =
        withinRealTime("the app to connect") {
            var open: ScriptedConnection? = null
            while (open == null) {
                open = synchronized(connections) { connections.lastOrNull { it.isOpen } }
                if (open == null) delay(POLL_INTERVAL)
            }
            open
        }

    private inner class ScriptedConnection {
        private val incoming = Channel<GameSocketIncomingMessage>(Channel.UNLIMITED)
        private val closeReason = CompletableDeferred<String?>()

        @Volatile
        var isOpen = true
            private set

        val connection: GameSocketConnection =
            mockk {
                every { incomingMessages } returns incoming.receiveAsFlow()
                coEvery { send(any()) } answers { synchronized(sent) { sent += firstArg<ClientMessage>() } }
                coEvery { close() } answers { close(reason = null) }
                coEvery { closeReason() } coAnswers { withTimeoutOrNull(QUIET_WINDOW) { closeReason.await() } }
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        val isDrained: Boolean get() = incoming.isEmpty

        suspend fun deliver(message: GameSocketIncomingMessage) = incoming.send(message)

        fun drop(reason: String?) = close(reason)

        private fun close(reason: String?) {
            isOpen = false
            closeReason.complete(reason)
            incoming.close()
        }
    }

    private companion object {
        val CATCH_UP_MARKER = Player("catch-up-marker", "Marker", PlayerColour.Lime)
        val QUIET_WINDOW = 200.milliseconds
        val POLL_INTERVAL = 5.milliseconds
    }
}
