package com.example.storyround.testing

import com.example.storyround.time.TimeProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class VirtualClock {
    private val scheduler = TestCoroutineScheduler()
    private val timerJob = SupervisorJob()
    private val timerScope = CoroutineScope(timerJob + UnconfinedTestDispatcher(scheduler))

    val nowEpochMillis: Long get() = START_EPOCH_MILLIS + scheduler.currentTime

    val timeProvider: TimeProvider = mockk { every { nowEpochMillis() } answers { nowEpochMillis } }

    val serverModule: Module =
        module {
            single<TimeProvider> { timeProvider }
            single<CoroutineScope> { timerScope }
        }

    fun advanceBy(duration: Duration) {
        scheduler.advanceTimeBy(duration)
        scheduler.runCurrent()
    }

    suspend fun <T> awaitTimerScheduledBy(action: suspend () -> T): T {
        val existing = timerJob.children.toSet()
        val result = action()
        awaitInRealTime("a new server timer to be scheduled") { timerJob.children.any { it !in existing && it.isActive } }
        return result
    }

    private companion object {
        const val START_EPOCH_MILLIS = 1_767_225_600_000L
    }
}
