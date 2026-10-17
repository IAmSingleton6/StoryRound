package com.example.storyround.testing

import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomSettings
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.protocol.protocolJson
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class PlayerRobot internal constructor(
    val name: String,
    private val client: HttpClient,
    private val findPlayer: (String) -> PlayerRobot?,
) {
    private val json = protocolJson()
    private var session: DefaultClientWebSocketSession? = null
    private var isClosedByServer = false
    private val inbox = mutableListOf<Received>()
    private val received = mutableListOf<ServerMessage>()
    private var nextSequence = 0L
    private var latestRoom: RoomState? = null
    private var knownPlayerId: String? = null
    private var knownSessionToken: String? = null
    private var knownRoomCode: String? = null

    val playerId: String get() = checkNotNull(knownPlayerId) { "$name has no session yet." }
    val roomCode: String get() = checkNotNull(knownRoomCode) { "$name is not in a room yet." }
    private val sessionToken: String get() = checkNotNull(knownSessionToken) { "$name has no session yet." }

    val otherDevice: PlayerRobot by lazy {
        PlayerRobot(name, client, findPlayer).also { device ->
            device.knownPlayerId = knownPlayerId
            device.knownSessionToken = knownSessionToken
            device.knownRoomCode = knownRoomCode
        }
    }

    suspend fun connect() {
        withinRealTime("$name to connect") { connection() }
    }

    suspend fun createRoom(withName: String = name) {
        send(ClientMessage.CreateRoom(withName))
        settleSession(expectsArrival = true)
    }

    suspend fun joinRoom(
        roomCode: String,
        withName: String = name,
    ) {
        send(ClientMessage.JoinRoom(roomCode, withName))
        settleSession(expectsArrival = true)
    }

    suspend fun reconnect() = reconnectWithToken(sessionToken)

    suspend fun reconnectWithToken(token: String) {
        send(ClientMessage.Reconnect(roomCode, playerId, token))
        settleSession(expectsArrival = false)
    }

    suspend fun startGame(
        opener: String = name,
        rounds: Int = 1,
        oneWordPerTurn: Boolean = false,
    ) = sendStartGame(idOf(opener), RoomSettings(rounds, oneWordPerTurn))

    suspend fun startGameWithRandomOpener(rounds: Int = 1) = sendStartGame(openingPlayerId = null, settings = RoomSettings(rounds))

    suspend fun startGameWithUnknownOpener() = sendStartGame(openingPlayerId = "not-in-this-room", settings = RoomSettings(rounds = 1))

    suspend fun submitPrompt(text: String) {
        awaitRoom("being asked for the opening prompt") { it.status == GameStatus.Prompt && it.openingPlayerId == knownPlayerId }
        trySubmitPrompt(text)
    }

    suspend fun trySubmitPrompt(text: String) {
        send(ClientMessage.SubmitPrompt(roomCode, playerId, text))
        settle("its prompt being accepted") { it is ServerMessage.RoomStateChanged && it.room.status == GameStatus.Playing }
    }

    suspend fun submitContribution(text: String) {
        awaitRoom("its turn to write") { it.status == GameStatus.Playing && it.currentPlayerId == knownPlayerId }
        trySubmitContribution(text)
    }

    suspend fun trySubmitContribution(text: String) {
        send(ClientMessage.SubmitContribution(roomCode, playerId, text))
        val accepted =
            settle("its contribution being added") {
                it is ServerMessage.ContributionAdded && it.contribution.playerId == knownPlayerId && it.contribution.text == text
            } ?: return
        take("the room with its contribution", before = accepted) { it is ServerMessage.RoomStateChanged }
    }

    suspend fun leaveRoom() {
        send(ClientMessage.LeaveRoom(roomCode, playerId))
        awaitServerCaughtUp()
    }

    suspend fun playAgain() {
        awaitRoom("the game finishing") { it.status == GameStatus.Finished }
        tryPlayAgain()
    }

    suspend fun tryPlayAgain() {
        send(ClientMessage.PlayAgain(roomCode, playerId))
        settle("the room returning to the lobby") { it is ServerMessage.RoomStateChanged && it.room.status == GameStatus.Lobby }
    }

    suspend fun startGameAs(
        playerName: String,
        inRoom: String = findPlayer(playerName)?.roomCode ?: roomCode,
    ) {
        val impersonated = idOf(playerName)
        send(ClientMessage.StartGame(inRoom, impersonated, impersonated, RoomSettings(rounds = 1)))
    }

    suspend fun sendRawText(text: String) {
        withinRealTime("$name to send a frame") { connection().send(Frame.Text(text)) }
    }

    suspend fun sendRawBinary(bytes: ByteArray) {
        withinRealTime("$name to send a frame") { connection().send(Frame.Binary(true, bytes)) }
    }

    suspend fun disconnect() {
        val open = session ?: return
        session = null
        withinRealTime("$name's connection to close") {
            open.close()
            open.coroutineContext.job.join()
        }
    }

    suspend fun assertRoom(
        description: String,
        matches: (RoomState) -> Boolean,
    ) {
        awaitRoom(description, matches)
    }

    suspend fun assertPlayers(vararg names: String) {
        awaitRoom("players ${names.toList()}") { room -> room.players.map { it.name } == names.toList() }
    }

    suspend fun assertHost(playerName: String) {
        awaitRoom("$playerName hosting") { room -> room.players.any { it.id == room.hostId && it.name == playerName } }
    }

    suspend fun assertStatus(status: GameStatus) {
        awaitRoom("the room in $status") { it.status == status }
    }

    suspend fun assertColourOf(
        playerName: String,
        colour: PlayerColour,
    ) {
        awaitRoom("$playerName in $colour") { room -> room.players.any { it.name == playerName && it.colour == colour } }
    }

    suspend fun assertEveryoneHasADifferentColour() {
        val colours = awaitRoom("a room") { true }.players.map { it.colour }
        assertEquals(colours.distinct(), colours, "Colours in $name's room")
    }

    suspend fun assertCurrentTurn(
        of: String,
        number: Int? = null,
        endingAt: Long? = null,
    ) {
        val id = idOf(of)
        val room =
            awaitRoom("$of's turn${number?.let { " $it" }.orEmpty()}") {
                it.status == GameStatus.Playing && it.currentPlayerId == id && (number == null || it.turnNumber == number)
            }
        endingAt?.let { assertEquals(it, room.turnDeadlineEpochMillis, "$of's turn deadline, as seen by $name") }
    }

    fun assertInDifferentRoomFrom(other: String) = assertNotEquals(findPlayer(other)?.roomCode, roomCode, "$name's room code")

    suspend fun assertErrorReceived(code: String) {
        take("an ERROR $code") { it is ServerMessage.Error && it.code == code }
    }

    suspend fun assertPlayerJoined(playerName: String) {
        take("$playerName joining") { it is ServerMessage.PlayerJoined && it.player.name == playerName }
    }

    suspend fun assertPlayerLeft(playerName: String) {
        val id = idOf(playerName)
        take("$playerName leaving") { it is ServerMessage.PlayerLeft && it.playerId == id }
    }

    suspend fun assertTurnStarted(of: String) {
        val id = idOf(of)
        take("$of's turn starting") { it is ServerMessage.TurnStarted && it.playerId == id }
    }

    suspend fun assertTurnExpired(of: String) {
        val id = idOf(of)
        take("$of's turn expiring") { it is ServerMessage.TurnExpired && it.playerId == id }
    }

    suspend fun assertTurnNotExpired(
        of: String,
        window: Duration = QUIET_WINDOW,
    ) {
        val id = idOf(of)
        listenQuietly(window)
        check(inbox.none { (it.message as? ServerMessage.TurnExpired)?.playerId == id }) {
            "$name was told $of's turn expired. ${describeMessages()}"
        }
    }

    suspend fun assertContributionAdded(
        by: String,
        text: String,
    ) {
        val id = idOf(by)
        take("$by's contribution \"$text\"") {
            it is ServerMessage.ContributionAdded && it.contribution.playerId == id && it.contribution.text == text
        }
    }

    suspend fun assertStoryFinished(
        prompt: String,
        vararg contributions: String,
    ) {
        val finished = take("the finished story") { it is ServerMessage.GameFinished }.message as ServerMessage.GameFinished
        assertEquals(prompt, finished.room.prompt, "Finished prompt, as seen by $name")
        assertEquals(contributions.toList(), finished.room.contributions.map { it.text }, "Finished contributions, as seen by $name")
    }

    suspend fun assertNextMessages(vararg types: KClass<out ServerMessage>) {
        withinRealTime("$name to receive ${types.size} messages", describeFailure = ::describeMessages) {
            while (inbox.size < types.size) receiveNext()
        }
        val next = inbox.take(types.size)
        assertEquals(types.map { it.simpleName }, next.map { it.message::class.simpleName }, "Next messages for $name")
        inbox.removeAll(next)
    }

    suspend fun assertNoMoreMessages(window: Duration = QUIET_WINDOW) {
        listenQuietly(window)
        check(inbox.isEmpty()) { "$name received unexpected messages: ${inbox.map { it.message }}" }
    }

    suspend fun assertDisconnectedByServer() {
        withinRealTime("the server to close $name's connection", describeFailure = ::describeMessages) {
            while (!isClosedByServer) receiveNext()
        }
    }

    private suspend fun awaitServerCaughtUp() {
        sendRawText(CATCH_UP_PROBE)
        take("the server catching up") { it is ServerMessage.Error && it.code == INVALID_MESSAGE }
    }

    internal suspend fun awaitMessage(
        description: String,
        matches: (ServerMessage) -> Boolean,
    ) {
        take(description, matches = matches)
    }

    internal fun forgetReceived() = inbox.clear()

    private suspend fun sendStartGame(
        openingPlayerId: String?,
        settings: RoomSettings,
    ) {
        send(ClientMessage.StartGame(roomCode, playerId, openingPlayerId, settings))
        settle("the game starting") { it is ServerMessage.RoomStateChanged && it.room.status == GameStatus.Prompt }
    }

    private suspend fun settleSession(expectsArrival: Boolean) {
        val accepted = settle("a session") { it is ServerMessage.SessionEstablished } ?: return
        val established = accepted.message as ServerMessage.SessionEstablished
        knownPlayerId = established.playerId
        knownSessionToken = established.sessionToken
        val room = take("the room it joined", after = accepted) { it is ServerMessage.RoomStateChanged }
        knownRoomCode = (room.message as ServerMessage.RoomStateChanged).room.roomCode
        if (expectsArrival) {
            take("its own arrival", after = room) { it is ServerMessage.PlayerJoined && it.player.id == knownPlayerId }
        }
    }

    private suspend fun settle(
        description: String,
        success: (ServerMessage) -> Boolean,
    ): Received? {
        val reply = find(description) { success(it.message) || it.message is ServerMessage.Error }
        if (reply.message is ServerMessage.Error) return null
        inbox.remove(reply)
        return reply
    }

    private suspend fun take(
        description: String,
        after: Received? = null,
        before: Received? = null,
        matches: (ServerMessage) -> Boolean,
    ): Received {
        val found =
            find(description) { entry ->
                (after == null || entry.sequence > after.sequence) &&
                    (before == null || entry.sequence < before.sequence) &&
                    matches(entry.message)
            }
        inbox.remove(found)
        return found
    }

    private suspend fun find(
        description: String,
        matches: (Received) -> Boolean,
    ): Received =
        withinRealTime("$name to receive $description", describeFailure = ::describeMessages) {
            var match = inbox.firstOrNull(matches)
            while (match == null) {
                receiveNext()
                match = inbox.firstOrNull(matches)
            }
            match
        }

    private suspend fun awaitRoom(
        description: String,
        matches: (RoomState) -> Boolean,
    ): RoomState =
        withinRealTime("$name to see $description", describeFailure = ::describeMessages) {
            var room = latestRoom?.takeIf(matches)
            while (room == null) {
                receiveNext()
                room = latestRoom?.takeIf(matches)
            }
            room
        }

    private suspend fun listenQuietly(window: Duration) {
        withinRealTime("$name to listen for $window") {
            withTimeoutOrNull(window) { while (true) receiveNext() }
            Unit
        }
    }

    private suspend fun send(message: ClientMessage) {
        withinRealTime("$name to send $message") {
            connection().send(Frame.Text(json.encodeToString(ClientMessage.serializer(), message)))
        }
    }

    private suspend fun connection(): DefaultClientWebSocketSession =
        session ?: client.webSocketSession(GAME_PATH).also {
            session = it
            isClosedByServer = false
        }

    private suspend fun receiveNext() {
        val open = session
        if (open == null || isClosedByServer) {
            delay(IDLE_POLL_INTERVAL)
            return
        }
        val frame =
            try {
                open.incoming.receive()
            } catch (_: ClosedReceiveChannelException) {
                isClosedByServer = true
                return
            }
        if (frame !is Frame.Text) return
        val message = json.decodeFromString<ServerMessage>(frame.readText())
        received += message
        inbox += Received(nextSequence++, message)
        when (message) {
            is ServerMessage.RoomStateChanged -> latestRoom = message.room
            is ServerMessage.GameFinished -> latestRoom = message.room
            else -> Unit
        }
    }

    private suspend fun idOf(playerName: String): String {
        fun known() = latestRoom?.players?.firstOrNull { it.name == playerName }?.id ?: findPlayer(playerName)?.knownPlayerId
        known()?.let { return it }
        awaitRoom("a player called $playerName") { room -> room.players.any { it.name == playerName } }
        return checkNotNull(known())
    }

    private fun describeMessages(): String =
        "Latest room: $latestRoom. Unread: ${inbox.map { it.message }}. Last messages received: ${received.takeLast(DESCRIBED_MESSAGES)}."

    private data class Received(
        val sequence: Long,
        val message: ServerMessage,
    )

    private companion object {
        const val GAME_PATH = "/game"
        const val CATCH_UP_PROBE = "catch-up probe"
        const val INVALID_MESSAGE = "INVALID_MESSAGE"
        const val DESCRIBED_MESSAGES = 12
        val QUIET_WINDOW = 200.milliseconds
        val IDLE_POLL_INTERVAL = 5.milliseconds
    }
}
