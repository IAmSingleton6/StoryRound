package com.example.storyround

import com.example.storyround.config.ServerConfigProviderImpl
import com.example.storyround.config.SystemEnvironment
import com.example.storyround.di.serverModule
import com.example.storyround.game.GameWebSocketHandler
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
import org.koin.core.module.Module
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin

private const val MAX_WEBSOCKET_FRAME_BYTES = 16L * 1024
private const val MAX_HEADER_BYTES = 65_536

fun main() {
    val config = ServerConfigProviderImpl(SystemEnvironment()).load()
    embeddedServer(Netty, configure = {
        connectors.add(
            EngineConnectorBuilder().apply {
                host = config.host
                port = config.port
            },
        )
        maxHeaderSize = MAX_HEADER_BYTES
    }) {
        storyRoundServer()
    }.start(wait = true)
}

fun Application.storyRoundServer(overrides: List<Module> = emptyList()) {
    install(Koin) { modules(listOf(serverModule()) + overrides) }
    val gameWebSocketHandler: GameWebSocketHandler = get()
    install(WebSockets) { maxFrameSize = MAX_WEBSOCKET_FRAME_BYTES }

    routing {
        get("/") { call.respondText("Story Relay server") }
        webSocket("/game") { gameWebSocketHandler.handle(this) }
    }
}
