package com.example.storyround.testing.base

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.configuration.ServerUrlProvider
import com.example.storyround.random.RandomNumberGenerator
import com.example.storyround.testing.StoryRoundTestServer
import com.example.storyround.testing.WithTestServer
import com.example.storyround.testing.actor.AppRobot
import com.example.storyround.testing.actor.DeviceStorageRobot
import com.example.storyround.time.TimeProvider
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import kotlinx.coroutines.test.TestResult
import org.koin.dsl.module

abstract class FullStackTest {
    protected fun fullStackTest(
        configuration: GameConfiguration = GameConfiguration(),
        randomNumberGenerator: RandomNumberGenerator? = null,
        body: suspend FullStackTestScope.() -> Unit,
    ): TestResult =
        runComposeUiTest {
            val server = StoryRoundTestServer(configuration, randomNumberGenerator)
            server.start()
            val scope = FullStackTestScope(this, server)
            try {
                scope.body()
            } finally {
                scope.appRobot.close()
                server.stop()
            }
        }
}

class FullStackTestScope(
    ui: ComposeUiTest,
    override val server: StoryRoundTestServer,
) : ScreenRobots by ScreenRobotSet(ui),
    WithTestServer {
    val storageRobot = DeviceStorageRobot()

    val appRobot =
        AppRobot(ui) {
            listOf(
                module {
                    single<ServerUrlProvider> { ServerUrlProvider { GAME_URL } }
                    single<HttpClient> { server.createClient() }
                    single<Settings> { storageRobot.settings }
                    single<TimeProvider> { clock.timeProvider }
                },
            )
        }

    private companion object {
        const val GAME_URL = "ws://localhost/game"
    }
}
