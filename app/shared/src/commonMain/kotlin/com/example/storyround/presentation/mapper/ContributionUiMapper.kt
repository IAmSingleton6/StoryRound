package com.example.storyround.presentation.mapper

import com.example.storyround.presentation.model.ContributionUi
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.protocol.Contribution

internal interface ContributionUiMapper {
    fun map(
        contributions: List<Contribution>,
        playersById: Map<String, PlayerUi>,
    ): List<ContributionUi>
}
