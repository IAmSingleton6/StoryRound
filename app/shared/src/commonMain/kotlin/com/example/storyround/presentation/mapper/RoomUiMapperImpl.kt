package com.example.storyround.presentation.mapper

import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.RoomUi
import com.example.storyround.presentation.model.TurnUi
import com.example.storyround.protocol.RoomState

internal class RoomUiMapperImpl(
    private val playerUiMapper: PlayerUiMapper,
    private val contributionUiMapper: ContributionUiMapper,
) : RoomUiMapper {
    override fun map(room: RoomState): RoomUi {
        val players = room.players.map(playerUiMapper::map)
        val playersById = players.associateBy(PlayerUi::id)
        return RoomUi(
            roomCode = room.roomCode,
            hostId = room.hostId,
            players = players,
            prompt = room.prompt.orEmpty(),
            contributions = contributionUiMapper.map(room.contributions, playersById),
            turn = room.toTurnUi(playersById),
            promptAuthor = (room.openingPlayerId ?: room.hostId).let(playersById::get),
            maxRounds = room.maxRounds,
        )
    }

    private fun RoomState.toTurnUi(playersById: Map<String, PlayerUi>): TurnUi? =
        currentPlayerId
            ?.let(playersById::get)
            ?.let { player ->
                turnDeadlineEpochMillis?.let { deadline ->
                    TurnUi(player = player, number = turnNumber, deadlineEpochMillis = deadline)
                }
            }
}
