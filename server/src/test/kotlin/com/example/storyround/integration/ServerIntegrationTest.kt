package com.example.storyround.integration

import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.random.RandomNumberGenerator
import com.example.storyround.testing.BrowserRobot
import com.example.storyround.testing.StoryRoundTestServer
import com.example.storyround.testing.WithTestServer
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest

abstract class ServerIntegrationTest {
    protected fun serverTest(
        configuration: GameConfiguration = GameConfiguration(),
        randomNumberGenerator: RandomNumberGenerator? = null,
        body: suspend ServerTestScope.() -> Unit,
    ): TestResult =
        runTest {
            val server = StoryRoundTestServer(configuration, randomNumberGenerator)
            server.start()
            try {
                ServerTestScope(server).body()
            } finally {
                server.stop()
            }
        }
}

class ServerTestScope(
    override val server: StoryRoundTestServer,
) : WithTestServer {
    val browserRobot = BrowserRobot(server)
}
