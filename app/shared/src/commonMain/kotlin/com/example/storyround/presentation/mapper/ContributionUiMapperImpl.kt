package com.example.storyround.presentation.mapper

import com.example.storyround.presentation.model.ContributionUi
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.protocol.Contribution

internal class ContributionUiMapperImpl : ContributionUiMapper {
    override fun map(
        contributions: List<Contribution>,
        playersById: Map<String, PlayerUi>,
    ): List<ContributionUi> =
        contributions.mapNotNull { contribution ->
            playersById[contribution.playerId]?.let { player ->
                ContributionUi(
                    player = player,
                    text = contribution.text,
                    order = contribution.turnNumber,
                )
            }
        }
}
