package com.example.storyround.testing

interface WithTestServer {
    val server: StoryRoundTestServer

    val clock: VirtualClock get() = server.clock

    fun player(name: String): PlayerRobot = server.player(name)
}
