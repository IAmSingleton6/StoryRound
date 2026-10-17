package com.example.storyround.testing

import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.random.RandomNumberGenerator
import com.example.storyround.storyRoundServer
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.server.routing.routing
import io.ktor.server.testing.TestApplication
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.send
import org.koin.dsl.module

class StoryRoundTestServer(
    configuration: GameConfiguration = GameConfiguration(),
    randomNumberGenerator: RandomNumberGenerator? = null,
) {
    val clock = VirtualClock()
    private val players = linkedMapOf<String, PlayerRobot>()
    private val overrides =
        module {
            single { configuration }
            randomNumberGenerator?.let { generator -> single<RandomNumberGenerator> { generator } }
        }
    private val application =
        TestApplication {
            application {
                storyRoundServer(listOf(clock.serverModule, overrides))
                routing {
                    webSocket(UNREADABLE_FRAME_PATH) { send("this is not a game message") }
                }
            }
        }
    private val playerClient by lazy { createClient() }

    suspend fun start() = application.start()

    suspend fun stop() = application.stop()

    fun createClient(): HttpClient = application.createClient { install(WebSockets) }

    fun player(name: String): PlayerRobot = players.getOrPut(name) { PlayerRobot(name, playerClient, players::get) }

    companion object {
        const val UNREADABLE_FRAME_PATH = "/test/unreadable"
    }
}
