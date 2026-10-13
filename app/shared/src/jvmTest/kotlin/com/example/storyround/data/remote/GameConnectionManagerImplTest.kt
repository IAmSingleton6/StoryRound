package com.example.storyround.data.remote

import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.ServerMessage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GameConnectionManagerImplTest {
    private val socket = mockk<GameSocket>()
    private val message = GameSocketIncomingMessage.Received(ServerMessage.PlayerLeft("sam"))
    private val received = mutableListOf<GameSocketIncomingMessage>()
    private val disconnects = mutableListOf<String?>()
    private var connectedCallbacks = 0

    @Test
    fun `given the socket opens when connecting then the connection is reported and every message forwarded`() =
        runTest {
            coEvery { socket.connect(SERVER_URL) } returns connection(openFlow(message))
            val manager = GameConnectionManagerImpl(socket, backgroundScope)

            val result = connect(manager)
            runCurrent()

            assertEquals(GameConnectionResult.Connected, result)
            assertTrue(manager.isConnected)
            assertEquals(1, connectedCallbacks)
            assertEquals(listOf<GameSocketIncomingMessage>(message), received)
        }

    @Test
    fun `given the socket cannot open when connecting then the failure is returned`() =
        runTest {
            coEvery { socket.connect(SERVER_URL) } throws IllegalStateException("offline")
            val manager = GameConnectionManagerImpl(socket, backgroundScope)

            assertEquals(GameConnectionResult.Failed("offline"), connect(manager))
            assertFalse(manager.isConnected)
        }

    @Test
    fun `given an open connection when connecting again then the existing connection is kept`() =
        runTest {
            coEvery { socket.connect(SERVER_URL) } returns connection(openFlow())
            val manager = GameConnectionManagerImpl(socket, backgroundScope)
            connect(manager)

            assertEquals(GameConnectionResult.Connected, connect(manager))
            coVerify(exactly = 1) { socket.connect(SERVER_URL) }
        }

    @Test
    fun `given the server closes the socket when reading then the disconnect and its reason are reported`() =
        runTest {
            coEvery { socket.connect(SERVER_URL) } returns connection(flowOf(message), closeReason = "Server restarting")
            val manager = GameConnectionManagerImpl(socket, backgroundScope)

            connect(manager)
            runCurrent()

            assertFalse(manager.isConnected)
            assertEquals(listOf<String?>("Server restarting"), disconnects)
        }

    @Test
    fun `given an open connection when disconnected on purpose then it is closed without reporting a disconnect`() =
        runTest {
            val connection = connection(openFlow())
            coEvery { socket.connect(SERVER_URL) } returns connection
            val manager = GameConnectionManagerImpl(socket, backgroundScope)
            connect(manager)

            manager.disconnect()
            runCurrent()

            assertFalse(manager.isConnected)
            assertTrue(disconnects.isEmpty())
            coVerify(exactly = 1) { connection.close() }
        }

    @Test
    fun `given the connection state when sending then only a delivered message reports success`() =
        runTest {
            val connection = connection(openFlow())
            coEvery { socket.connect(SERVER_URL) } returns connection
            val manager = GameConnectionManagerImpl(socket, backgroundScope)
            val createRoom = ClientMessage.CreateRoom("Alex")

            assertFalse(manager.send(createRoom))
            connect(manager)
            assertTrue(manager.send(createRoom))
            coEvery { connection.send(any()) } throws IllegalStateException("Socket closed")
            assertFalse(manager.send(createRoom))
        }

    private suspend fun connect(manager: GameConnectionManager): GameConnectionResult =
        manager.connect(
            serverUrl = SERVER_URL,
            onConnected = { connectedCallbacks++ },
            onIncomingMessage = { received += it },
            onDisconnected = { disconnects += it },
        )

    private fun openFlow(vararg messages: GameSocketIncomingMessage): Flow<GameSocketIncomingMessage> =
        flow {
            messages.forEach { emit(it) }
            awaitCancellation()
        }

    private fun connection(
        incoming: Flow<GameSocketIncomingMessage>,
        closeReason: String? = null,
    ): GameSocketConnection =
        mockk(relaxed = true) {
            every { incomingMessages } returns incoming
            coEvery { closeReason() } returns closeReason
        }

    private companion object {
        const val SERVER_URL = "ws://localhost:8080/game"
    }
}
