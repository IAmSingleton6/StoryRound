package com.example.storyround.presentation.model

import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.MAX_STORY_TEXT_LENGTH
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.SavedStory

internal const val MAX_CONTRIBUTION_LENGTH = MAX_STORY_TEXT_LENGTH

internal data class PlayerUi(
    val id: String,
    val name: String,
    val colour: PlayerColour,
)

internal data class ContributionUi(
    val player: PlayerUi,
    val text: String,
    val order: Int,
)

internal data class StoryUi(
    val id: String,
    val prompt: String,
    val players: List<PlayerUi>,
    val contributions: List<ContributionUi>,
)

internal data class TurnUi(
    val player: PlayerUi,
    val number: Int,
    val deadlineEpochMillis: Long,
)

internal data class RoomUi(
    val roomCode: String,
    val hostId: String,
    val players: List<PlayerUi>,
    val prompt: String,
    val contributions: List<ContributionUi>,
    val turn: TurnUi?,
)

internal fun RoomState.toRoomUi(): RoomUi {
    val uiPlayers =
        players.map { player ->
            PlayerUi(id = player.id, name = player.name, colour = player.colour)
        }
    val playersById = uiPlayers.associateBy(PlayerUi::id)
    return RoomUi(
        roomCode = roomCode,
        hostId = hostId,
        players = uiPlayers,
        prompt = prompt.orEmpty(),
        contributions = contributions.toContributionUi(playersById),
        turn =
            currentPlayerId
                ?.let(playersById::get)
                ?.let { player ->
                    turnDeadlineEpochMillis?.let { deadline ->
                        TurnUi(player = player, number = turnNumber, deadlineEpochMillis = deadline)
                    }
                },
    )
}

internal fun SavedStory.toStoryUi(): StoryUi {
    val uiPlayers =
        players.map { player ->
            PlayerUi(id = player.id, name = player.name, colour = player.colour)
        }
    return StoryUi(
        id = id,
        prompt = prompt,
        players = uiPlayers,
        contributions = contributions.toContributionUi(uiPlayers.associateBy(PlayerUi::id)),
    )
}

private fun List<Contribution>.toContributionUi(playersById: Map<String, PlayerUi>): List<ContributionUi> =
    mapNotNull { contribution ->
        playersById[contribution.playerId]?.let { player ->
            ContributionUi(player = player, text = contribution.text, order = contribution.turnNumber)
        }
    }
