package com.example.storyround

import com.example.storyround.config.EnvironmentServerConfigProvider
import com.example.storyround.di.serverModule
import com.example.storyround.game.GameWebSocketHandler
import com.example.storyround.history.StoryHistoryRouteInstaller
import com.example.storyround.persistence.StoryRepository
import com.example.storyround.room.InMemoryRoomRegistry
import com.example.storyround.room.RoomRegistry
import com.example.storyround.time.SystemTimeProvider
import com.example.storyround.time.TimeProvider
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun main() {
    val config = EnvironmentServerConfigProvider().load()
    embeddedServer(Netty, port = config.port, host = config.host) {
        module()
    }.start(wait = true)
}

fun Application.module(
    registry: RoomRegistry? = null,
    storyRepository: StoryRepository? = null,
    timeProvider: TimeProvider = SystemTimeProvider(),
) {
    val roomRegistry = registry ?: InMemoryRoomRegistry(timeProvider = timeProvider)
    install(Koin) { modules(serverModule(roomRegistry, storyRepository, timeProvider)) }
    val gameWebSocketHandler: GameWebSocketHandler by inject()
    val storyHistoryRoutes: StoryHistoryRouteInstaller by inject()
    install(WebSockets)

    routing {
        get("/") { call.respondText("Story Relay server") }
        storyHistoryRoutes.install(this)
        webSocket("/game") { gameWebSocketHandler.handle(this) }
    }
}
