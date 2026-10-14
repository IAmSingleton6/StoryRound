package com.example.storyround

import com.example.storyround.config.EnvironmentServerConfigProvider
import com.example.storyround.di.serverModule
import com.example.storyround.game.GameWebSocketHandler
import com.example.storyround.room.RoomRegistry
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.EngineConnectorBuilder
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

private const val MAX_WEBSOCKET_FRAME_BYTES = 16L * 1024
private const val MAX_HEADER_BYTES = 65_536

fun main() {
    val config = EnvironmentServerConfigProvider().load()
    embeddedServer(Netty, configure = {
        connectors.add(
            EngineConnectorBuilder().apply {
                host = config.host
                port = config.port
            },
        )
        maxHeaderSize = MAX_HEADER_BYTES
    }) {
        module()
    }.start(wait = true)
}

internal fun Application.module(registry: RoomRegistry? = null) {
    install(Koin) { modules(serverModule(registry)) }
    val gameWebSocketHandler: GameWebSocketHandler by inject()
    install(WebSockets) { maxFrameSize = MAX_WEBSOCKET_FRAME_BYTES }

    routing {
        get("/") { call.respondText("Story Relay server") }
        webSocket("/game") { gameWebSocketHandler.handle(this) }
    }
}
