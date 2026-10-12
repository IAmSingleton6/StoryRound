package com.example.storyround.game

import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.RoomState
import com.example.storyround.room.RegistryResult
import com.example.storyround.room.RoomRegistry
import com.example.storyround.time.TimeProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TurnExpirySchedulerImplTest {
    private val registry = mockk<RoomRegistry>()
    private val timeProvider = mockk<TimeProvider> { every { nowEpochMillis() } returns 1_000L }
    private val room =
        RoomState("AB12", "host", emptyList(), GameStatus.Playing, turnDeadlineEpochMillis = 16_000L, maxPlayers = 10, maxRounds = 1)

    @Test
    fun `given a turn with a deadline when the deadline passes then the turn is expired and the result handed on`() =
        runTest {
            val expiry = RegistryResult.Success(room.copy(turnNumber = 1), null)
            every { registry.expireTurn("AB12", 16_000L) } returns expiry
            val expired = mutableListOf<RegistryResult.Success>()
            val scheduler = TurnExpirySchedulerImpl(registry, backgroundScope, timeProvider)

            scheduler.schedule(room) { expired += it }
            advanceTimeBy(14_999L)
            runCurrent()
            assertTrue(expired.isEmpty())

            advanceTimeBy(2L)
            runCurrent()
            assertEquals(listOf(expiry), expired)
        }

    @Test
    fun `given the player contributed before the deadline when it passes then nothing is handed on`() =
        runTest {
            every { registry.expireTurn("AB12", 16_000L) } returns null
            val expired = mutableListOf<RegistryResult.Success>()

            TurnExpirySchedulerImpl(registry, backgroundScope, timeProvider).schedule(room) { expired += it }
            advanceTimeBy(15_001L)
            runCurrent()

            assertTrue(expired.isEmpty())
            verify(exactly = 1) { registry.expireTurn("AB12", 16_000L) }
        }

    @Test
    fun `given a room without a running turn when scheduled then no expiry is attempted`() =
        runTest {
            TurnExpirySchedulerImpl(registry, backgroundScope, timeProvider).schedule(room.copy(turnDeadlineEpochMillis = null)) {}
            advanceTimeBy(60_000L)
            runCurrent()

            verify(exactly = 0) { registry.expireTurn(any(), any()) }
        }
}
