package com.example.storyround.data.remote

import com.example.storyround.logging.Logger
import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.ServerMessage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
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
    private val logger = mockk<Logger>(relaxed = true)
    private val message = GameSocketIncomingMessage.Received(ServerMessage.PlayerLeft("sam"))
    private val received = mutableListOf<GameSocketIncomingMessage>()
    private val disconnects = mutableListOf<String?>()
    private var connectedCallbacks = 0

    @Test
    fun `given the socket opens when connecting then the connection is reported and every message forwarded`() =
        runTest {
            // Given
            coEvery { socket.connect(SERVER_URL) } returns connection(openFlow(message))
            val manager = GameConnectionManagerImpl(socket, backgroundScope, logger)

            // When
            val result = connect(manager)
            runCurrent()

            // Then
            assertEquals(GameConnectionResult.Connected, result)
            assertTrue(manager.isConnected)
            assertEquals(1, connectedCallbacks)
            assertEquals(listOf<GameSocketIncomingMessage>(message), received)
        }

    @Test
    fun `given the socket cannot open when connecting then the failure is returned`() =
        runTest {
            // Given
            coEvery { socket.connect(SERVER_URL) } throws IllegalStateException("offline")
            val manager = GameConnectionManagerImpl(socket, backgroundScope, logger)

            // When
            val result = connect(manager)

            // Then
            assertEquals(GameConnectionResult.Failed("offline"), result)
            assertFalse(manager.isConnected)
        }

    @Test
    fun `given an open connection when connecting again then the existing connection is kept`() =
        runTest {
            // Given
            coEvery { socket.connect(SERVER_URL) } returns connection(openFlow())
            val manager = GameConnectionManagerImpl(socket, backgroundScope, logger)
            connect(manager)

            // When
            val result = connect(manager)

            // Then
            assertEquals(GameConnectionResult.Connected, result)
            coVerify(exactly = 1) { socket.connect(SERVER_URL) }
        }

    @Test
    fun `given a connection is still opening when connecting again then only one socket is opened`() =
        runTest {
            // Given
            val opened = CompletableDeferred<GameSocketConnection>()
            coEvery { socket.connect(SERVER_URL) } coAnswers { opened.await() }
            val manager = GameConnectionManagerImpl(socket, backgroundScope, logger)
            val first = async { connect(manager) }
            val second = async { connect(manager) }
            runCurrent()

            // When
            opened.complete(connection(openFlow()))
            val results = listOf(first.await(), second.await())

            // Then
            assertEquals(listOf(GameConnectionResult.Connected, GameConnectionResult.Connected), results)
            assertEquals(1, connectedCallbacks)
            coVerify(exactly = 1) { socket.connect(SERVER_URL) }
        }

    @Test
    fun `given the server closes the socket when reading then the disconnect and its reason are reported`() =
        runTest {
            // Given
            coEvery { socket.connect(SERVER_URL) } returns connection(flowOf(message), closeReason = "Server restarting")
            val manager = GameConnectionManagerImpl(socket, backgroundScope, logger)

            // When
            connect(manager)
            runCurrent()

            // Then
            assertFalse(manager.isConnected)
            assertEquals(listOf<String?>("Server restarting"), disconnects)
        }

    @Test
    fun `given an open connection when disconnected on purpose then it is closed without reporting a disconnect`() =
        runTest {
            // Given
            val connection = connection(openFlow())
            coEvery { socket.connect(SERVER_URL) } returns connection
            val manager = GameConnectionManagerImpl(socket, backgroundScope, logger)
            connect(manager)

            // When
            manager.disconnect()
            runCurrent()

            // Then
            assertFalse(manager.isConnected)
            assertTrue(disconnects.isEmpty())
            coVerify(exactly = 1) { connection.close() }
        }

    @Test
    fun `given the connection state when sending then only a delivered message reports success`() =
        runTest {
            // Given
            val connection = connection(openFlow())
            coEvery { socket.connect(SERVER_URL) } returns connection
            val manager = GameConnectionManagerImpl(socket, backgroundScope, logger)
            val createRoom = ClientMessage.CreateRoom("Alex")

            // When
            val sentBeforeConnecting = manager.send(createRoom)
            connect(manager)
            val sentWhileConnected = manager.send(createRoom)
            coEvery { connection.send(any()) } throws IllegalStateException("Socket closed")
            val sentAfterSocketFailed = manager.send(createRoom)

            // Then
            assertFalse(sentBeforeConnecting)
            assertTrue(sentWhileConnected)
            assertFalse(sentAfterSocketFailed)
        }

    @Test
    fun `given an open connection when the socket fails while reading then the failure is logged and a disconnect reported`() =
        runTest {
            // Given
            val failure = IllegalStateException("Connection reset")
            coEvery { socket.connect(SERVER_URL) } returns connection(flow { throw failure })
            val manager = GameConnectionManagerImpl(socket, backgroundScope, logger)

            // When
            connect(manager)
            runCurrent()

            // Then
            verify { logger.warn(any(), match { it.contains("dropped") }, failure) }
            assertEquals(listOf<String?>(null), disconnects)
        }

    @Test
    fun `given an open connection when a message cannot be sent then the failure is logged`() =
        runTest {
            // Given
            val connection = connection(openFlow())
            val failure = IllegalStateException("Socket closed")
            coEvery { socket.connect(SERVER_URL) } returns connection
            coEvery { connection.send(any()) } throws failure
            val manager = GameConnectionManagerImpl(socket, backgroundScope, logger)
            connect(manager)

            // When
            manager.send(ClientMessage.CreateRoom("Alex"))

            // Then
            verify { logger.warn(any(), match { it.contains("CreateRoom") }, failure) }
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
