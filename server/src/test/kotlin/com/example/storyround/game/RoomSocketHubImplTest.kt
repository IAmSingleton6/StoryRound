package com.example.storyround.game

import com.example.storyround.logging.Logger
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.protocol.protocolJson
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoomSocketHubImplTest {
    private val json = protocolJson()
    private val logger = mockk<Logger>(relaxed = true)
    private val hub = RoomSocketHubImpl(json, logger)
    private val message = ServerMessage.PlayerLeft("sam")
    private val alexConnection = session()
    private val alexNewConnection = session()
    private val samConnection = session()

    @Test
    fun `given sessions in two rooms when a room is broadcast to then only its sessions receive the message`() =
        runTest {
            // Given
            val first = session()
            val second = session()
            val elsewhere = session()
            hub.bind(first, "AB12", "alex")
            hub.bind(second, "AB12", "sam")
            hub.bind(elsewhere, "CD34", "jo")

            // When
            hub.broadcast("AB12", message)

            // Then
            coVerify(exactly = 1) { first.send(match<Frame> { it.isMessage() }) }
            coVerify(exactly = 1) { second.send(match<Frame> { it.isMessage() }) }
            coVerify(exactly = 0) { elsewhere.send(any()) }
        }

    @Test
    fun `given a bound session when it binds to another room then it moves and its previous binding is returned`() =
        runTest {
            // Given
            val session = session()
            hub.bind(session, "AB12", "alex")

            // When
            val previous = hub.bind(session, "CD34", "alex")
            hub.broadcast("AB12", message)

            // Then
            assertEquals(PlayerBinding("AB12", "alex"), previous)
            assertEquals(PlayerBinding("CD34", "alex"), hub.bindingOf(session))
            assertFalse(hub.hasConnections("AB12"))
            coVerify(exactly = 0) { session.send(any()) }
        }

    @Test
    fun `given the last session in a room when it is unbound then the room has no connections and gets nothing`() =
        runTest {
            // Given
            val session = session()
            hub.bind(session, "AB12", "alex")

            // When
            val binding = hub.unbind(session)
            hub.broadcast("AB12", message)

            // Then
            assertEquals(PlayerBinding("AB12", "alex"), binding)
            assertNull(hub.bindingOf(session))
            assertFalse(hub.hasConnections("AB12"))
            assertNull(hub.unbind(session))
            coVerify(exactly = 0) { session.send(any()) }
        }

    @Test
    fun `should release only that player's connection when a player is unbound`() =
        runTest {
            // Given
            hub.bind(alexConnection, "AB12", "alex")
            hub.bind(samConnection, "AB12", "sam")

            // When
            hub.unbindPlayer("AB12", "alex")

            // Then
            assertNull(hub.bindingOf(alexConnection))
            assertEquals(PlayerBinding("AB12", "sam"), hub.bindingOf(samConnection))
            assertTrue(hub.hasConnections("AB12"))
        }

    @Test
    fun `should release the player's old connection when the player binds on a new one`() =
        runTest {
            // Given
            hub.bind(alexConnection, "AB12", "alex")

            // When
            hub.bind(alexNewConnection, "AB12", "alex")

            // Then
            assertNull(hub.bindingOf(alexConnection))
            assertEquals(PlayerBinding("AB12", "alex"), hub.bindingOf(alexNewConnection))

            hub.broadcast("AB12", message)
            coVerify(exactly = 0) { alexConnection.send(any()) }
            coVerify(exactly = 1) { alexNewConnection.send(match<Frame> { it.isMessage() }) }
        }

    @Test
    fun `given a room with sessions when the room is unbound then all of its sessions are released`() {
        // Given
        val first = session()
        val second = session()
        hub.bind(first, "AB12", "alex")
        hub.bind(second, "AB12", "sam")

        // When
        hub.unbindRoom("AB12")

        // Then
        assertNull(hub.bindingOf(first))
        assertNull(hub.bindingOf(second))
        assertFalse(hub.hasConnections("AB12"))
    }

    @Test
    fun `given a session that can no longer be written to when sent a message then it is closed`() =
        runTest {
            // Given
            val session = session()
            coEvery { session.send(match<Frame> { it is Frame.Text }) } throws IllegalStateException("Socket closed")

            // When
            hub.send(session, message)

            // Then
            coVerify(exactly = 1) { session.send(match<Frame> { it is Frame.Close }) }
        }

    @Test
    fun `given a seated player whose session can no longer be written to when sent a message then the failure is logged`() =
        runTest {
            // Given
            val session = session()
            val failure = IllegalStateException("Socket closed")
            hub.bind(session, "AB12", "alex")
            coEvery { session.send(match<Frame> { it is Frame.Text }) } throws failure

            // When
            hub.send(session, message)

            // Then
            verify { logger.warn(any(), match { it.contains("alex") && it.contains("AB12") }, failure) }
        }

    private fun session() = mockk<DefaultWebSocketServerSession>(relaxed = true)

    private fun Frame.isMessage(): Boolean = this is Frame.Text && json.decodeFromString(ServerMessage.serializer(), readText()) == message
}
