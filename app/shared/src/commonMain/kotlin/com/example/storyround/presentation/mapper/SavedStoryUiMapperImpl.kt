package com.example.storyround.presentation.mapper

import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.StoryUi
import com.example.storyround.protocol.SavedStory

internal class SavedStoryUiMapperImpl(
    private val playerUiMapper: PlayerUiMapper,
    private val contributionUiMapper: ContributionUiMapper,
) : SavedStoryUiMapper {
    override fun map(story: SavedStory): StoryUi {
        val players = story.players.map(playerUiMapper::map)
        val playersById = players.associateBy(PlayerUi::id)
        return StoryUi(
            id = story.id,
            prompt = story.prompt,
            players = players,
            contributions = contributionUiMapper.map(story.contributions, playersById),
            promptAuthor = playersById[story.promptPlayerId],
        )
    }
}
