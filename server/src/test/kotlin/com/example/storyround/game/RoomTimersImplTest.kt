package com.example.storyround.game

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class RoomTimersImplTest {
    private val fired = mutableListOf<String>()

    @Test
    fun `given a scheduled timer when its delay passes then its action runs once`() =
        runTest {
            // Given
            val timers = RoomTimersImpl(backgroundScope)

            // When
            timers.schedule("AB12", RoomTimer.TurnExpiry, 1_000L) { fired += "expiry" }
            advanceAndRun(999L)
            assertEquals(emptyList(), fired)

            // Then
            advanceAndRun(2L)
            assertEquals(listOf("expiry"), fired)
        }

    @Test
    fun `given a pending timer when the same timer is scheduled again then only the replacement runs`() =
        runTest {
            // Given
            val timers = RoomTimersImpl(backgroundScope)

            // When
            timers.schedule("AB12", RoomTimer.TurnExpiry, 1_000L) { fired += "first" }
            timers.schedule("AB12", RoomTimer.TurnExpiry, 2_000L) { fired += "second" }
            advanceAndRun(5_000L)

            // Then
            assertEquals(listOf("second"), fired)
        }

    @Test
    fun `given timers of different kinds and rooms when one is cancelled then the others still run`() =
        runTest {
            // Given
            val timers = RoomTimersImpl(backgroundScope)

            // When
            timers.schedule("AB12", RoomTimer.TurnExpiry, 1_000L) { fired += "AB12 expiry" }
            timers.schedule("AB12", RoomTimer.IdleClose, 1_000L) { fired += "AB12 idle" }
            timers.schedule("CD34", RoomTimer.TurnExpiry, 1_000L) { fired += "CD34 expiry" }
            timers.cancel("AB12", RoomTimer.TurnExpiry)
            advanceAndRun(5_000L)

            // Then
            assertEquals(setOf("AB12 idle", "CD34 expiry"), fired.toSet())
        }

    @Test
    fun `given a room with pending timers when all are cancelled then none of them run`() =
        runTest {
            // Given
            val timers = RoomTimersImpl(backgroundScope)

            // When
            timers.schedule("AB12", RoomTimer.TurnExpiry, 1_000L) { fired += "expiry" }
            timers.schedule("AB12", RoomTimer.IdleClose, 1_000L) { fired += "idle" }
            timers.cancelAll("AB12")
            advanceAndRun(5_000L)

            // Then
            assertEquals(emptyList(), fired)
        }

    @Test
    fun `given a firing timer when its action schedules the next one then the action completes and the next one runs`() =
        runTest {
            // Given
            val timers = RoomTimersImpl(backgroundScope)

            // When
            timers.schedule("AB12", RoomTimer.TurnExpiry, 1_000L) {
                timers.schedule("AB12", RoomTimer.TurnExpiry, 1_000L) { fired += "second turn" }
                yield()
                fired += "first turn"
            }
            advanceAndRun(1_001L)
            assertEquals(listOf("first turn"), fired)

            // Then
            advanceAndRun(1_000L)
            assertEquals(listOf("first turn", "second turn"), fired)
        }

    @Test
    fun `given a deadline already in the past when scheduled then the action runs straight away`() =
        runTest {
            // Given
            val timers = RoomTimersImpl(backgroundScope)

            // When
            timers.schedule("AB12", RoomTimer.TurnExpiry, -500L) { fired += "expiry" }
            runCurrent()

            // Then
            assertEquals(listOf("expiry"), fired)
        }

    private fun TestScope.advanceAndRun(millis: Long) {
        advanceTimeBy(millis)
        runCurrent()
    }
}
