package com.example.storyround.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.storyround.protocol.MAX_STORY_TEXT_LENGTH
import com.example.storyround.protocol.MAX_WORD_LENGTH
import com.example.storyround.ui.preview.ComponentPreview
import com.example.storyround.ui.preview.PreviewStory
import com.example.storyround.ui.theme.CardShape
import com.example.storyround.ui.theme.OutlineWidth
import com.example.storyround.ui.theme.StoryRoundTheme
import org.jetbrains.compose.resources.stringResource
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.ic_dice
import storyround.app.shared.generated.resources.ic_send
import storyround.app.shared.generated.resources.writing_field_idea
import storyround.app.shared.generated.resources.writing_field_idea_action

private val IdeaButtonShape = RoundedCornerShape(percent = 50)
private val DraftFieldHeight = 76.dp
private val IdeaButtonHeight = 38.dp
private const val NEAR_LIMIT_MARGIN = 20
private const val NEAR_LIMIT_SHARE_OF_MAX = 6

@Composable
internal fun WritingField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    maxLength: Int,
    sendLabel: String,
    canSend: Boolean,
    onSend: () -> Unit,
    onRandomIdea: (() -> Unit)?,
    modifier: Modifier = Modifier,
    error: String? = null,
    singleWord: Boolean = false,
) {
    val colors = StoryRoundTheme.colors
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(colors.paper, CardShape)
                .border(OutlineWidth.Regular, colors.ink, CardShape)
                .padding(start = 16.dp, top = 12.dp, end = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DraftTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            singleWord = singleWord,
            onKeyboardSend = { if (canSend) onSend() },
        )
        WritingFieldFooter(
            length = value.length,
            maxLength = maxLength,
            error = error,
            sendLabel = sendLabel,
            canSend = canSend,
            onSend = onSend,
            onRandomIdea = onRandomIdea,
        )
    }
}

@Composable
private fun DraftTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleWord: Boolean,
    onKeyboardSend: () -> Unit,
) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    var editedValue by remember { mutableStateOf(valueWithCursorAtEnd(value)) }
    val displayedValue = if (editedValue.text == value) editedValue else valueWithCursorAtEnd(value)
    BasicTextField(
        value = displayedValue,
        onValueChange = { updated ->
            editedValue = updated
            if (updated.text != value) onValueChange(updated.text)
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .then(if (singleWord) Modifier else Modifier.height(DraftFieldHeight))
                .focusRequester(focusRequester)
                .semantics { contentDescription = placeholder },
        textStyle = typography.storyInput.copy(color = colors.ink),
        keyboardOptions = keyboardOptions(singleWord),
        keyboardActions = KeyboardActions(onSend = { onKeyboardSend() }),
        singleLine = singleWord,
        cursorBrush = SolidColor(colors.ink),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    BasicText(placeholder, style = typography.storyInput.copy(color = colors.onPaperMuted))
                }
                innerTextField()
            }
        },
    )
}

private fun valueWithCursorAtEnd(text: String) = TextFieldValue(text, TextRange(text.length))

private fun keyboardOptions(singleWord: Boolean): KeyboardOptions =
    if (singleWord) {
        KeyboardOptions(capitalization = KeyboardCapitalization.None, imeAction = ImeAction.Send)
    } else {
        KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Default)
    }

private fun isNearLimit(
    length: Int,
    maxLength: Int,
): Boolean = length >= maxLength - minOf(NEAR_LIMIT_MARGIN, maxLength / NEAR_LIMIT_SHARE_OF_MAX)

@Composable
private fun WritingFieldFooter(
    length: Int,
    maxLength: Int,
    error: String?,
    sendLabel: String,
    canSend: Boolean,
    onSend: () -> Unit,
    onRandomIdea: (() -> Unit)?,
) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (error != null) {
            BasicText(
                text = error,
                modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite },
                style = typography.counter.copy(color = colors.coralInk),
            )
        } else {
            BasicText(
                text = "$length/$maxLength",
                modifier = Modifier.weight(1f),
                style = typography.counter.copy(color = if (isNearLimit(length, maxLength)) colors.coralInk else colors.onPaperMuted),
            )
        }
        onRandomIdea?.let { IdeaButton(it) }
        RaisedButton(
            label = sendLabel,
            onClick = onSend,
            size = RaisedButtonSize.Compact,
            enabled = canSend,
            shadowColor = colors.ink,
            modifier = Modifier.padding(start = 10.dp),
            trailingIcon = Res.drawable.ic_send,
            contentPadding = PaddingValues(start = 20.dp, end = 16.dp),
            textStyle = typography.buttonSmall,
        )
    }
}

@Composable
private fun IdeaButton(onClick: () -> Unit) {
    val colors = StoryRoundTheme.colors
    Row(
        modifier =
            Modifier
                .height(IdeaButtonHeight)
                .border(OutlineWidth.Thin, colors.ink, IdeaButtonShape)
                .clickable(role = Role.Button, onClickLabel = stringResource(Res.string.writing_field_idea_action), onClick = onClick)
                .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TintedIcon(Res.drawable.ic_dice, tint = colors.ink, modifier = Modifier.size(16.dp))
        BasicText(stringResource(Res.string.writing_field_idea), style = StoryRoundTheme.typography.labelMedium.copy(color = colors.ink))
    }
}

@Preview
@Composable
private fun WritingFieldEmptyPreview() = ComponentPreview { WritingFieldPreviewContent(value = "", maxLength = MAX_STORY_TEXT_LENGTH) }

@Preview
@Composable
private fun WritingFieldDraftPreview() =
    ComponentPreview { WritingFieldPreviewContent(value = PreviewStory.draft, maxLength = MAX_STORY_TEXT_LENGTH) }

@Preview
@Composable
private fun WritingFieldNearLimitPreview() =
    ComponentPreview { WritingFieldPreviewContent(value = PreviewStory.draft, maxLength = PreviewStory.draft.length + 5) }

@Preview
@Composable
private fun WritingFieldOneWordErrorPreview() =
    ComponentPreview {
        WritingField(
            value = "a horse",
            onValueChange = {},
            placeholder = "Add one word…",
            maxLength = MAX_WORD_LENGTH,
            sendLabel = "SUBMIT",
            canSend = false,
            onSend = {},
            onRandomIdea = null,
            error = "One word only, no spaces",
            singleWord = true,
        )
    }

@Composable
private fun WritingFieldPreviewContent(
    value: String,
    maxLength: Int,
) = WritingField(
    value = value,
    onValueChange = {},
    placeholder = "Continue the story…",
    maxLength = maxLength,
    sendLabel = "SUBMIT",
    canSend = value.isNotBlank(),
    onSend = {},
    onRandomIdea = {},
)
