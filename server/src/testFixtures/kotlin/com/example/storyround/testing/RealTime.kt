package com.example.storyround.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

val ROBOT_TIMEOUT: Duration = 5.seconds

private val POLL_INTERVAL = 5.milliseconds

suspend fun <T : Any> withinRealTime(
    waitingFor: String,
    timeout: Duration = ROBOT_TIMEOUT,
    describeFailure: () -> String = { "" },
    block: suspend () -> T,
): T =
    withContext(Dispatchers.Default) {
        withTimeoutOrNull(timeout) { block() }
    } ?: throw AssertionError("Timed out after $timeout waiting for $waitingFor. ${describeFailure()}".trimEnd())

suspend fun awaitInRealTime(
    waitingFor: String,
    condition: () -> Boolean,
) {
    withinRealTime(waitingFor) {
        while (!condition()) delay(POLL_INTERVAL)
    }
}
