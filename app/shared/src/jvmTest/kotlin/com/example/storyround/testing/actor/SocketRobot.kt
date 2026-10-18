package com.example.storyround.testing.actor

import com.example.storyround.data.remote.GameSocketConnection
import com.example.storyround.data.remote.GameSocketIncomingMessage
import com.example.storyround.data.remote.KtorGameSocket
import com.example.storyround.logging.RecordingLogger
import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.protocol.protocolJson
import com.example.storyround.testing.StoryRoundTestServer
import com.example.storyround.testing.withinRealTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SocketRobot(
    private val server: StoryRoundTestServer,
) {
    private lateinit var connection: GameSocketConnection
    private var lastMessage: GameSocketIncomingMessage? = null

    suspend fun connect(url: String) {
        connection = KtorGameSocket(server.createClient(), protocolJson(), RecordingLogger()).connect(url)
    }

    suspend fun send(message: ClientMessage) = connection.send(message)

    suspend fun readNextMessage() {
        lastMessage = withinRealTime("the server's next message") { connection.incomingMessages.first() }
    }

    suspend fun assertSessionEstablished() {
        val reply = withinRealTime("the server's reply") { connection.incomingMessages.first() }
        val received = assertIs<GameSocketIncomingMessage.Received>(reply)
        assertIs<ServerMessage.SessionEstablished>(received.message)
    }

    fun assertLastMessageUnreadable() {
        assertIs<GameSocketIncomingMessage.Unreadable>(lastMessage)
    }

    suspend fun assertClosedWithReason() {
        withinRealTime("the connection to close") { connection.incomingMessages.toList() }
        val reason = connection.closeReason()
        assertTrue(reason?.isNotBlank() == true, "Expected a close reason, got $reason")
    }
}
