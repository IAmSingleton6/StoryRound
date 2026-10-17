package com.example.storyround.room

import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.ErrorCode
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.MAX_PLAYER_NAME_LENGTH
import com.example.storyround.protocol.MAX_STORY_TEXT_LENGTH
import com.example.storyround.protocol.MAX_WORD_LENGTH
import com.example.storyround.protocol.MIN_PLAYER_NAME_LENGTH
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomSettings
import com.example.storyround.protocol.UserTextValidator
import com.example.storyround.random.RandomNumberGenerator
import com.example.storyround.rules.TurnRules
import com.example.storyround.time.TimeProvider

internal class RoomRegistryImpl(
    private val roomStore: RoomStore,
    private val identityGenerator: RoomIdentityGenerator,
    private val timeProvider: TimeProvider,
    private val turnRules: TurnRules,
    private val roomStateMapper: RoomStateMapper,
    private val gameConfiguration: GameConfiguration,
    private val randomNumberGenerator: RandomNumberGenerator,
    private val userTextValidator: UserTextValidator,
) : RoomRegistry {
    override fun createRoom(playerName: String): RegistryResult {
        val validatedName =
            userTextValidator.validPlayerName(playerName)
                ?: return INVALID_NAME
        repeat(MAX_CODE_GENERATION_ATTEMPTS) {
            val host = Player(identityGenerator.generatePlayerId(), validatedName, PlayerColour.entries.first())
            val sessionToken = identityGenerator.generateSessionToken()
            val room =
                MutableGameRoom(
                    code = identityGenerator.generateRoomCode().uppercase(),
                    hostId = host.id,
                    players = mutableListOf(host),
                    maxPlayers = gameConfiguration.maxPlayers,
                    sessionTokens = mutableMapOf(host.id to sessionToken),
                    settings = RoomSettings(rounds = gameConfiguration.defaultRounds),
                )
            if (roomStore.create(room)) {
                return RegistryResult.Success(roomStateMapper.map(room), RoomEvent.RoomCreated(host, sessionToken))
            }
        }
        error("Unable to allocate a unique room code.")
    }

    override fun joinRoom(
        roomCode: String,
        playerName: String,
    ): RegistryResult =
        roomStore.withRoom(roomCode) { room ->
            room ?: return@withRoom RegistryResult.Error(ErrorCode.ROOM_NOT_FOUND, "No game found with that code. Check it and try again.")
            if (room.status != GameStatus.Lobby) {
                return@withRoom RegistryResult.Error(ErrorCode.GAME_ALREADY_STARTED, "This game has already started.")
            }
            if (room.players.size >= room.maxPlayers) {
                return@withRoom RegistryResult.Error(ErrorCode.ROOM_FULL, "This room already has ${room.maxPlayers} players.")
            }
            val validatedName =
                userTextValidator.validPlayerName(playerName)
                    ?: return@withRoom INVALID_NAME
            val player = Player(identityGenerator.generatePlayerId(), validatedName, room.firstFreeColour())
            val sessionToken = identityGenerator.generateSessionToken()
            room.players += player
            room.sessionTokens[player.id] = sessionToken
            RegistryResult.Success(roomStateMapper.map(room), RoomEvent.PlayerJoined(player, sessionToken))
        }

    override fun reconnect(
        roomCode: String,
        playerId: String,
        sessionToken: String,
    ): RegistryResult =
        withExistingRoom(roomCode) { room ->
            val player =
                room.players.firstOrNull { it.id == playerId }
                    ?: return@withExistingRoom PLAYER_NOT_FOUND
            if (room.sessionTokens[playerId] != sessionToken) {
                return@withExistingRoom RegistryResult.Error(ErrorCode.INVALID_SESSION, "That session cannot reconnect to this player.")
            }
            RegistryResult.Success(roomStateMapper.map(room), RoomEvent.PlayerReconnected(player, sessionToken))
        }

    override fun startGame(
        roomCode: String,
        playerId: String,
        openingPlayerId: String?,
        settings: RoomSettings,
    ): RegistryResult =
        withExistingRoom(roomCode) { room ->
            if (room.hostId != playerId) {
                return@withExistingRoom RegistryResult.Error(ErrorCode.NOT_HOST, "Only the host can start the game.")
            }
            if (room.status != GameStatus.Lobby) {
                return@withExistingRoom RegistryResult.Error(ErrorCode.INVALID_GAME_STATE, "The game has already started.")
            }
            if (settings.rounds !in gameConfiguration.minRounds..gameConfiguration.maxRounds) {
                return@withExistingRoom RegistryResult.Error(
                    ErrorCode.INVALID_ROUNDS,
                    "Choose between ${gameConfiguration.minRounds} and ${gameConfiguration.maxRounds} rounds.",
                )
            }
            val selectedOpeningPlayerId =
                openingPlayerId ?: room.players[randomNumberGenerator.nextInt(room.players.size)].id
            if (room.players.none { it.id == selectedOpeningPlayerId }) {
                return@withExistingRoom RegistryResult.Error(
                    ErrorCode.INVALID_PROMPT_OWNER,
                    "Choose a player in this room to start the story.",
                )
            }
            room.openingPlayerId = selectedOpeningPlayerId
            room.settings = settings
            room.status = GameStatus.Prompt
            RegistryResult.Success(roomStateMapper.map(room), RoomEvent.GameStarted)
        }

    override fun submitPrompt(
        roomCode: String,
        playerId: String,
        text: String,
    ): RegistryResult =
        withExistingRoom(roomCode) { room ->
            if (room.status != GameStatus.Prompt) {
                return@withExistingRoom RegistryResult.Error(ErrorCode.INVALID_GAME_STATE, "The game is not waiting for an opening prompt.")
            }
            if (room.openingPlayerId != playerId) {
                return@withExistingRoom RegistryResult.Error(ErrorCode.NOT_PROMPT_OWNER, "Only the selected player can start the story.")
            }
            val prompt =
                userTextValidator.validStoryText(text, room.settings)
                    ?: return@withExistingRoom invalidStoryText(room.settings)
            room.prompt = prompt
            room.status = GameStatus.Playing
            val firstTurn = startTurn(room, 0)
            RegistryResult.Success(roomStateMapper.map(room), RoomEvent.PromptSubmitted(firstTurn))
        }

    override fun submitContribution(
        roomCode: String,
        playerId: String,
        text: String,
    ): RegistryResult =
        withExistingRoom(roomCode) { room ->
            if (room.status != GameStatus.Playing) {
                return@withExistingRoom RegistryResult.Error(ErrorCode.INVALID_GAME_STATE, "The game is not accepting contributions.")
            }
            if (requireNotNull(room.turnDeadlineEpochMillis) <= timeProvider.nowEpochMillis()) {
                return@withExistingRoom RegistryResult.Error(ErrorCode.TURN_EXPIRED, "That turn has already ended.")
            }
            if (room.currentPlayerId != playerId) {
                return@withExistingRoom RegistryResult.Error(ErrorCode.NOT_CURRENT_PLAYER, "It is not your turn.")
            }
            val contributionText =
                userTextValidator.validStoryText(text, room.settings)
                    ?: return@withExistingRoom invalidStoryText(room.settings)
            val contribution = Contribution(playerId, contributionText, room.turnNumber)
            room.contributions += contribution
            val outcome = advance(room)
            RegistryResult.Success(roomStateMapper.map(room), RoomEvent.ContributionAdded(contribution, outcome))
        }

    override fun playAgain(
        roomCode: String,
        playerId: String,
    ): RegistryResult =
        withExistingRoom(roomCode) { room ->
            if (room.hostId != playerId) {
                return@withExistingRoom RegistryResult.Error(ErrorCode.NOT_HOST, "Only the host can start another game.")
            }
            if (room.status != GameStatus.Finished) {
                return@withExistingRoom RegistryResult.Error(
                    ErrorCode.INVALID_GAME_STATE,
                    "Finish the current game before starting another.",
                )
            }
            room.status = GameStatus.Lobby
            room.prompt = null
            room.openingPlayerId = null
            room.contributions.clear()
            room.currentPlayerId = null
            room.turnNumber = 0
            room.turnDeadlineEpochMillis = null
            RegistryResult.Success(roomStateMapper.map(room), RoomEvent.GameReset)
        }

    override fun expireTurn(
        roomCode: String,
        deadlineEpochMillis: Long,
    ): RegistryResult.Success? =
        roomStore.withRoom(roomCode) { room ->
            room ?: return@withRoom null
            if (
                room.status != GameStatus.Playing ||
                room.turnDeadlineEpochMillis != deadlineEpochMillis ||
                timeProvider.nowEpochMillis() < deadlineEpochMillis
            ) {
                return@withRoom null
            }
            val expiredPlayerId = requireNotNull(room.currentPlayerId)
            val outcome = advance(room)
            RegistryResult.Success(roomStateMapper.map(room), RoomEvent.TurnExpired(expiredPlayerId, outcome))
        }

    override fun closeRoom(roomCode: String): RegistryResult.Success? =
        roomStore.withRoom(roomCode) { room ->
            room ?: return@withRoom null
            roomStore.remove(room.code)
            RegistryResult.Success(roomStateMapper.map(room), RoomEvent.RoomClosed)
        }

    override fun leaveRoom(
        roomCode: String,
        playerId: String,
    ): RegistryResult =
        withExistingRoom(roomCode) { room ->
            if (room.status != GameStatus.Lobby) {
                return@withExistingRoom RegistryResult.Error(
                    ErrorCode.CANNOT_LEAVE_GAME,
                    "You can only leave while the game is in the lobby.",
                )
            }
            if (room.players.none { it.id == playerId }) {
                return@withExistingRoom PLAYER_NOT_FOUND
            }
            room.players.removeAll { it.id == playerId }
            room.sessionTokens.remove(playerId)
            if (room.players.isNotEmpty() && room.hostId == playerId) room.hostId = room.players.first().id
            if (room.players.isEmpty()) {
                roomStore.remove(room.code)
                return@withExistingRoom RegistryResult.Success(roomStateMapper.map(room), RoomEvent.RoomClosed)
            }
            RegistryResult.Success(roomStateMapper.map(room), RoomEvent.PlayerLeft(playerId))
        }

    private fun withExistingRoom(
        roomCode: String,
        block: (MutableGameRoom) -> RegistryResult,
    ): RegistryResult = roomStore.withRoom(roomCode) { room -> room?.let(block) ?: ROOM_NOT_FOUND }

    private fun advance(room: MutableGameRoom): TurnOutcome {
        val nextTurnNumber = room.turnNumber + 1
        if (turnRules.isComplete(nextTurnNumber, room.players.size, room.settings.rounds)) {
            room.status = GameStatus.Finished
            room.currentPlayerId = null
            room.turnDeadlineEpochMillis = null
            return TurnOutcome.GameFinished
        }
        return startTurn(room, nextTurnNumber)
    }

    private fun startTurn(
        room: MutableGameRoom,
        turnNumber: Int,
    ): TurnOutcome.NextTurn {
        val playerId = room.players[turnRules.playerIndex(turnNumber, room.players.size)].id
        val deadlineEpochMillis = timeProvider.nowEpochMillis() + turnRules.durationMillis(turnNumber)
        room.turnNumber = turnNumber
        room.currentPlayerId = playerId
        room.turnDeadlineEpochMillis = deadlineEpochMillis
        return TurnOutcome.NextTurn(playerId, deadlineEpochMillis)
    }

    private fun MutableGameRoom.firstFreeColour(): PlayerColour =
        PlayerColour.entries.first { colour ->
            players.none { it.colour == colour }
        }
}

private const val MAX_CODE_GENERATION_ATTEMPTS = 100
private val ROOM_NOT_FOUND = RegistryResult.Error(ErrorCode.ROOM_NOT_FOUND, "That room does not exist.")
private val PLAYER_NOT_FOUND = RegistryResult.Error(ErrorCode.PLAYER_NOT_FOUND, "That player is not in this room.")
private val INVALID_NAME =
    RegistryResult.Error(ErrorCode.INVALID_NAME, "Enter a player name of $MIN_PLAYER_NAME_LENGTH to $MAX_PLAYER_NAME_LENGTH characters.")
private val INVALID_TEXT = RegistryResult.Error(ErrorCode.INVALID_TEXT, "Enter up to $MAX_STORY_TEXT_LENGTH characters.")
private val INVALID_WORD =
    RegistryResult.Error(ErrorCode.INVALID_WORD, "Enter one word of up to $MAX_WORD_LENGTH characters, with no spaces.")

private fun invalidStoryText(settings: RoomSettings): RegistryResult.Error = if (settings.oneWordPerTurn) INVALID_WORD else INVALID_TEXT
