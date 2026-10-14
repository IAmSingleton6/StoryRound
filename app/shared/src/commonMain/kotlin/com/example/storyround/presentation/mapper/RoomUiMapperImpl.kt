package com.example.storyround.presentation.mapper

import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.RoomUi
import com.example.storyround.presentation.model.TurnUi
import com.example.storyround.protocol.RoomState

internal class RoomUiMapperImpl(
    private val playerUiMapper: PlayerUiMapper,
    private val contributionUiMapper: ContributionUiMapper,
    private val storySegmentMapper: StorySegmentMapper,
) : RoomUiMapper {
    override fun map(room: RoomState): RoomUi {
        val players = room.players.map(playerUiMapper::map)
        val playersById = players.associateBy(PlayerUi::id)
        val prompt = room.prompt.orEmpty()
        val promptAuthor = playersById[room.openingPlayerId ?: room.hostId]
        val contributions = contributionUiMapper.map(room.contributions, playersById)
        val segments = storySegmentMapper.segments(prompt, promptAuthor, contributions)
        return RoomUi(
            roomCode = room.roomCode,
            hostId = room.hostId,
            players = players,
            maxPlayers = room.maxPlayers,
            prompt = prompt,
            contributions = contributions,
            turn = turn(room, playersById),
            maxRounds = room.settings.rounds,
            promptAuthor = promptAuthor,
            segments = segments,
            writers = storySegmentMapper.writers(players, segments),
        )
    }

    private fun turn(
        room: RoomState,
        playersById: Map<String, PlayerUi>,
    ): TurnUi? {
        val player = room.currentPlayerId?.let(playersById::get) ?: return null
        val deadline = room.turnDeadlineEpochMillis ?: return null
        return TurnUi(player = player, number = room.turnNumber, deadlineEpochMillis = deadline)
    }
}
