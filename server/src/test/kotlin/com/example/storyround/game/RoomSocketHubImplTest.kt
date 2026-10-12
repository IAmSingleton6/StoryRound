package com.example.storyround.game

import com.example.storyround.protocol.ServerMessage
import com.example.storyround.protocol.protocolJson
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class RoomSocketHubImplTest {
    private val json = protocolJson()
    private val hub = RoomSocketHubImpl(json)
    private val message = ServerMessage.PlayerLeft("sam")

    @Test
    fun `given sessions in two rooms when a room is broadcast to then only its sessions receive the message`() =
        runTest {
            val first = session()
            val second = session()
            val elsewhere = session()
            hub.subscribe("AB12", first)
            hub.subscribe("AB12", second)
            hub.subscribe("CD34", elsewhere)

            hub.broadcast("AB12", message)

            coVerify(exactly = 1) { first.send(match<Frame> { it.isMessage() }) }
            coVerify(exactly = 1) { second.send(match<Frame> { it.isMessage() }) }
            coVerify(exactly = 0) { elsewhere.send(any()) }
        }

    @Test
    fun `given an unsubscribed session when its room is broadcast to then it receives nothing`() =
        runTest {
            val session = session()
            hub.subscribe("AB12", session)

            hub.unsubscribe(session)
            hub.broadcast("AB12", message)

            coVerify(exactly = 0) { session.send(any()) }
        }

    @Test
    fun `given a session that can no longer be written to when sent a message then it is closed`() =
        runTest {
            val session = session()
            coEvery { session.send(match<Frame> { it is Frame.Text }) } throws IllegalStateException("Socket closed")

            hub.send(session, message)

            coVerify(exactly = 1) { session.send(match<Frame> { it is Frame.Close }) }
        }

    private fun session() = mockk<DefaultWebSocketServerSession>(relaxed = true)

    private fun Frame.isMessage(): Boolean = this is Frame.Text && json.decodeFromString(ServerMessage.serializer(), readText()) == message
}
