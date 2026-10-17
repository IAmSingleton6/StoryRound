package com.example.storyround.testing

import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.ServerMessage

private const val ANY_PROMPT = "A door creaked open."

suspend fun WithTestServer.roomInLobby(
    host: String,
    guests: List<String> = emptyList(),
) {
    player(host).createRoom()
    guests.forEach { guest -> player(guest).joinRoom(player(host).roomCode) }
    guests.lastOrNull()?.let { lastGuest ->
        (listOf(host) + guests.dropLast(1)).forEach { name ->
            player(name).awaitMessage("$lastGuest joining") { it is ServerMessage.PlayerJoined && it.player.name == lastGuest }
        }
    }
    forgetEverythingReceived(host, guests)
}

suspend fun WithTestServer.gameAwaitingPrompt(
    host: String,
    guests: List<String> = emptyList(),
    opener: String = host,
    rounds: Int = 1,
    oneWordPerTurn: Boolean = false,
) {
    roomInLobby(host, guests)
    player(host).startGame(opener, rounds, oneWordPerTurn)
    guests.forEach { guest ->
        player(guest).awaitMessage("the game starting") { it is ServerMessage.RoomStateChanged && it.room.status == GameStatus.Prompt }
    }
    forgetEverythingReceived(host, guests)
}

suspend fun WithTestServer.gameInPlay(
    host: String,
    guests: List<String> = emptyList(),
    opener: String = host,
    rounds: Int = 1,
    prompt: String = ANY_PROMPT,
    oneWordPerTurn: Boolean = false,
) {
    gameAwaitingPrompt(host, guests, opener, rounds, oneWordPerTurn)
    player(opener).submitPrompt(prompt)
    (listOf(host) + guests).forEach { name ->
        player(name).awaitMessage("the first turn starting") { it is ServerMessage.TurnStarted }
    }
    forgetEverythingReceived(host, guests)
}

suspend fun WithTestServer.gameFinished(
    host: String,
    guests: List<String> = emptyList(),
    rounds: Int = 1,
    prompt: String = ANY_PROMPT,
    lines: List<String> = List((guests.size + 1) * rounds) { turn -> "Line ${turn + 1}." },
) {
    val everyone = listOf(host) + guests
    val turns = everyone.size * rounds
    require(lines.size == turns) { "A game of ${everyone.size} players over $rounds rounds needs $turns lines." }
    gameInPlay(host, guests, rounds = rounds, prompt = prompt)
    lines.forEachIndexed { turn, line -> player(everyone[turn % everyone.size]).submitContribution(line) }
    everyone.forEach { name ->
        player(name).awaitMessage("the game finishing") { it is ServerMessage.GameFinished }
    }
    forgetEverythingReceived(host, guests)
}

private fun WithTestServer.forgetEverythingReceived(
    host: String,
    guests: List<String>,
) = (listOf(host) + guests).forEach { player(it).forgetReceived() }
