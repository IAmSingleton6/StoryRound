package com.example.storyround.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.theme.StoryRoundTheme

private val ComposerShape = RoundedCornerShape(20.dp)

@Composable
internal fun StoryComposer(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    maxLength: Int,
    isNearLimit: Boolean,
    sendLabel: String,
    canSend: Boolean,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(colors.paper, ComposerShape)
                .border(2.5.dp, colors.ink, ComposerShape)
                .padding(start = 16.dp, top = 12.dp, end = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(76.dp).semantics { contentDescription = placeholder },
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = "${value.length}/$maxLength",
                style = typography.counter.copy(color = if (isNearLimit) colors.coralInk else colors.onPaperMuted),
            )
            SrButton(
                label = sendLabel,
                onClick = onSend,
                size = SrButtonSize.Compact,
                enabled = canSend,
                shadowColor = colors.ink,
                trailingIcon = SrIcons.Send,
                contentPadding = PaddingValues(start = 20.dp, end = 16.dp),
                textStyle = typography.sendButton,
            )
        }
    }
}
