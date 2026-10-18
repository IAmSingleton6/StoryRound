package com.example.storyround.testing.base

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.storyround.configuration.ServerUrlProvider
import com.example.storyround.data.remote.FixedIntervalReconnectPolicy
import com.example.storyround.data.remote.GameSocket
import com.example.storyround.data.remote.ReconnectPolicy
import com.example.storyround.random.RandomNumberGenerator
import com.example.storyround.testing.actor.AppRobot
import com.example.storyround.testing.actor.DeviceClock
import com.example.storyround.testing.actor.DeviceStorageRobot
import com.example.storyround.testing.actor.ScriptedServerRobot
import com.example.storyround.time.TimeProvider
import com.russhwolf.settings.Settings
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.TestResult
import org.koin.dsl.module

abstract class AppFlowTest {
    protected fun appFlowTest(body: suspend AppFlowTestScope.() -> Unit): TestResult =
        runComposeUiTest {
            val scope = AppFlowTestScope(this)
            try {
                scope.body()
            } finally {
                scope.appRobot.close()
            }
        }
}

class AppFlowTestScope(
    ui: ComposeUiTest,
) : ScreenRobots by ScreenRobotSet(ui) {
    val serverRobot = ScriptedServerRobot()
    val storageRobot = DeviceStorageRobot()
    val clock = DeviceClock()
    private val firstChoice: RandomNumberGenerator = mockk { every { nextInt(any()) } returns 0 }

    val appRobot =
        AppRobot(ui) {
            listOf(
                module {
                    single<ServerUrlProvider> { ServerUrlProvider { "ws://scripted.test/game" } }
                    single<GameSocket> { serverRobot.gameSocket }
                    single<Settings> { storageRobot.settings }
                    single<TimeProvider> { clock.timeProvider }
                    single<RandomNumberGenerator> { firstChoice }
                    single<ReconnectPolicy> { FixedIntervalReconnectPolicy(intervalMillis = RECONNECT_INTERVAL_MILLIS) }
                },
            )
        }

    private companion object {
        const val RECONNECT_INTERVAL_MILLIS = 50L
    }
}
