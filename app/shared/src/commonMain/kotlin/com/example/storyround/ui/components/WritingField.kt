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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.theme.StoryRoundTheme

private val WritingFieldShape = RoundedCornerShape(20.dp)
private val IdeaButtonShape = RoundedCornerShape(19.dp)

@Composable
internal fun WritingField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    maxLength: Int,
    isNearLimit: Boolean,
    sendLabel: String,
    canSend: Boolean,
    onSend: () -> Unit,
    onRandomIdea: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    var fieldValue by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    val shownValue = if (fieldValue.text == value) fieldValue else TextFieldValue(value, TextRange(value.length))
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(colors.paper, WritingFieldShape)
                .border(2.5.dp, colors.ink, WritingFieldShape)
                .padding(start = 16.dp, top = 12.dp, end = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BasicTextField(
            value = shownValue,
            onValueChange = { updated ->
                fieldValue = updated
                if (updated.text != value) onValueChange(updated.text)
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .focusRequester(focusRequester)
                    .semantics { contentDescription = placeholder },
            textStyle = typography.storyInput.copy(color = colors.ink),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Default),
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
        WritingFieldFooter(
            length = value.length,
            maxLength = maxLength,
            isNearLimit = isNearLimit,
            sendLabel = sendLabel,
            canSend = canSend,
            onSend = onSend,
            onRandomIdea = onRandomIdea,
        )
    }
}

@Composable
private fun WritingFieldFooter(
    length: Int,
    maxLength: Int,
    isNearLimit: Boolean,
    sendLabel: String,
    canSend: Boolean,
    onSend: () -> Unit,
    onRandomIdea: () -> Unit,
) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            text = "$length/$maxLength",
            modifier = Modifier.weight(1f),
            style = typography.counter.copy(color = if (isNearLimit) colors.coralInk else colors.onPaperMuted),
        )
        IdeaButton(onRandomIdea)
        RaisedButton(
            label = sendLabel,
            onClick = onSend,
            size = RaisedButtonSize.Compact,
            enabled = canSend,
            shadowColor = colors.ink,
            modifier = Modifier.padding(start = 10.dp),
            trailingIcon = StoryRoundIcons.Send,
            contentPadding = PaddingValues(start = 20.dp, end = 16.dp),
            textStyle = typography.sendButton,
        )
    }
}

@Composable
private fun IdeaButton(onClick: () -> Unit) {
    val colors = StoryRoundTheme.colors
    Row(
        modifier =
            Modifier
                .height(38.dp)
                .border(2.dp, colors.ink, IdeaButtonShape)
                .clickable(role = Role.Button, onClickLabel = "Replace your text with a random idea", onClick = onClick)
                .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TintedIcon(StoryRoundIcons.Dice, tint = colors.ink, modifier = Modifier.size(16.dp))
        BasicText("Idea", style = StoryRoundTheme.typography.pill.copy(color = colors.ink))
    }
}
