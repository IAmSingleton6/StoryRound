package com.example.storyround.presentation.mapper

import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.StoryUi
import com.example.storyround.protocol.SavedStory

internal class SavedStoryUiMapperImpl(
    private val playerUiMapper: PlayerUiMapper,
    private val contributionUiMapper: ContributionUiMapper,
    private val storySegmentMapper: StorySegmentMapper,
) : SavedStoryUiMapper {
    override fun map(story: SavedStory): StoryUi {
        val players = story.players.map(playerUiMapper::map)
        val playersById = players.associateBy(PlayerUi::id)
        val promptAuthor = playersById[story.promptPlayerId]
        val contributions = contributionUiMapper.map(story.contributions, playersById)
        val segments = storySegmentMapper.segments(story.prompt, promptAuthor, contributions)
        return StoryUi(
            id = story.id,
            prompt = story.prompt,
            players = players,
            contributions = contributions,
            promptAuthor = promptAuthor,
            segments = segments,
            writers = storySegmentMapper.writers(players, segments),
        )
    }
}
