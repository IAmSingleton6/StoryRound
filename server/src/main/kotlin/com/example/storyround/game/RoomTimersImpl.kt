package com.example.storyround.game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

internal class RoomTimersImpl(
    private val scope: CoroutineScope,
) : RoomTimers {
    private val jobs = ConcurrentHashMap<TimerKey, Job>()

    override fun schedule(
        roomCode: String,
        timer: RoomTimer,
        delayMillis: Long,
        action: suspend () -> Unit,
    ) {
        val key = TimerKey(roomCode, timer)
        lateinit var job: Job
        job =
            scope.launch(start = CoroutineStart.LAZY) {
                delay(delayMillis.coerceAtLeast(0))
                jobs.remove(key, job)
                action()
            }
        jobs.put(key, job)?.cancel()
        job.start()
    }

    override fun cancel(
        roomCode: String,
        timer: RoomTimer,
    ) {
        jobs.remove(TimerKey(roomCode, timer))?.cancel()
    }

    override fun cancelAll(roomCode: String) {
        RoomTimer.entries.forEach { timer -> cancel(roomCode, timer) }
    }

    private data class TimerKey(
        val roomCode: String,
        val timer: RoomTimer,
    )
}
