package com.example.storyround.testing.fixtures

import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomSettings
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.SavedStory

internal val alexPlayer = Player("alex", "Alex", PlayerColour.Blue)
internal val samPlayer = Player("sam", "Sam", PlayerColour.Green)

internal const val ROOM_CODE = "7F2K"

internal fun tokenOf(player: Player) = "token-${player.id}"

internal fun lobbyRoom(
    players: List<Player> = listOf(alexPlayer),
    host: Player = alexPlayer,
    settings: RoomSettings = RoomSettings(rounds = 1),
) = RoomState(
    roomCode = ROOM_CODE,
    hostId = host.id,
    players = players,
    status = GameStatus.Lobby,
    maxPlayers = 10,
    settings = settings,
)

internal fun promptRoom(
    opener: Player,
    players: List<Player> = listOf(alexPlayer, samPlayer),
    settings: RoomSettings = RoomSettings(rounds = 1),
) = lobbyRoom(players, settings = settings).copy(status = GameStatus.Prompt, openingPlayerId = opener.id)

internal fun playingRoom(
    writer: Player,
    deadlineEpochMillis: Long,
    turnNumber: Int = 0,
    prompt: String = "A door opened.",
    contributions: List<Contribution> = emptyList(),
    players: List<Player> = listOf(alexPlayer, samPlayer),
    settings: RoomSettings = RoomSettings(rounds = 1),
) = promptRoom(opener = alexPlayer, players = players, settings = settings).copy(
    status = GameStatus.Playing,
    prompt = prompt,
    contributions = contributions,
    currentPlayerId = writer.id,
    turnNumber = turnNumber,
    turnDeadlineEpochMillis = deadlineEpochMillis,
)

internal fun finishedRoom(
    prompt: String = "A door opened.",
    contributions: List<Contribution> =
        listOf(Contribution(alexPlayer.id, "A horse walked in.", 0), Contribution(samPlayer.id, "It wore a hat.", 1)),
    players: List<Player> = listOf(alexPlayer, samPlayer),
) = promptRoom(opener = alexPlayer, players = players).copy(
    status = GameStatus.Finished,
    prompt = prompt,
    contributions = contributions,
    turnNumber = contributions.size,
)

internal fun savedStoryOf(
    prompt: String,
    createdAtEpochMillis: Long = 1L,
) = SavedStory(
    id = "saved-$prompt",
    prompt = prompt,
    promptPlayerId = alexPlayer.id,
    createdAtEpochMillis = createdAtEpochMillis,
    players = listOf(alexPlayer, samPlayer),
    contributions = listOf(Contribution(samPlayer.id, "Then it rained.", 0)),
)
