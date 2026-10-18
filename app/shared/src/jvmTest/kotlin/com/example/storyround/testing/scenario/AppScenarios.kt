package com.example.storyround.testing.scenario

import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.testing.base.FullStackTestScope

suspend fun FullStackTestScope.appHostingLobby(
    host: String,
    guests: List<String> = emptyList(),
) {
    appRobot.launch()
    homeRobot.enterName(host)
    homeRobot.tapCreateGame()
    val roomCode = lobbyRobot.readRoomCode()
    guests.forEach { guest -> player(guest).joinRoom(roomCode) }
    lobbyRobot.assertPlayersShown(host, *guests.toTypedArray())
}

suspend fun FullStackTestScope.appHostingGame(
    host: String,
    guests: List<String> = emptyList(),
    opener: String = host,
    rounds: Int = 1,
) {
    appHostingLobby(host, guests)
    lobbyRobot.selectOpener(if (opener == host) "You" else opener)
    val defaultRounds = GameConfiguration().defaultRounds
    repeat(defaultRounds - rounds) { lobbyRobot.tapFewerRounds() }
    repeat(rounds - defaultRounds) { lobbyRobot.tapMoreRounds() }
    lobbyRobot.tapStartGame()
    if (opener == host) openingPromptRobot.assertAskedForPrompt() else openingPromptRobot.assertWaitingFor(opener)
}

suspend fun FullStackTestScope.appFinishedGame(
    host: String,
    guests: List<String> = emptyList(),
    prompt: String = "A door opened.",
) {
    appHostingGame(host, guests, opener = host, rounds = 1)
    openingPromptRobot.enterPrompt(prompt)
    openingPromptRobot.tapStartStory()
    (listOf(host) + guests).forEachIndexed { turn, writer ->
        val line = "Line ${turn + 1}."
        if (writer == host) {
            playingRobot.enterContribution(line)
            playingRobot.tapSubmit()
        } else {
            player(writer).submitContribution(line)
        }
    }
    finishedRobot.assertShown()
}

suspend fun FullStackTestScope.appJoinedLobby(
    guest: String,
    host: String,
) {
    player(host).createRoom()
    appRobot.launch()
    homeRobot.enterName(guest)
    homeRobot.tapJoinGame()
    homeRobot.enterRoomCode(player(host).roomCode)
    homeRobot.tapJoinGame()
    lobbyRobot.assertWaitingForHost(host)
}
