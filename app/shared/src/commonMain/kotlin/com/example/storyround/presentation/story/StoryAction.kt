package com.example.storyround.presentation.story

internal sealed interface StoryAction {
    data class OpeningPromptChanged(
        val value: String,
    ) : StoryAction

    data object SubmitOpeningPrompt : StoryAction

    data class ContributionChanged(
        val value: String,
    ) : StoryAction

    data object SubmitContribution : StoryAction
}
