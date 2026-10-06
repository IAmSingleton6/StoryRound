package com.example.storyround.data.remote

internal class StoryHistoryEndpointResolver {
    fun resolve(
        serverUrl: String,
        path: String,
    ): String {
        require(path.startsWith('/'))
        val httpUrl =
            serverUrl
                .replaceFirst("ws://", "http://")
                .replaceFirst("wss://", "https://")
        return "${httpUrl.removeSuffix("/game")}$path"
    }
}
