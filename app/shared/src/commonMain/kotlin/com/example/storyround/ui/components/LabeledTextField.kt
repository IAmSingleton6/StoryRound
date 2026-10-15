package com.example.storyround.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.preview.ComponentPreview
import com.example.storyround.ui.preview.PreviewSurface
import com.example.storyround.ui.theme.OutlineWidth
import com.example.storyround.ui.theme.StoryRoundTheme

private val LabeledTextFieldShape = RoundedCornerShape(14.dp)

@Composable
internal fun LabeledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = StoryRoundTheme.typography.input,
    height: Dp = 56.dp,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val colors = StoryRoundTheme.colors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BasicText(label.uppercase(), style = StoryRoundTheme.typography.eyebrowMedium.copy(color = colors.onPaperMuted))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(height)
                    .semantics { contentDescription = label },
            textStyle = textStyle.copy(color = colors.ink),
            keyboardOptions = keyboardOptions,
            singleLine = true,
            cursorBrush = SolidColor(colors.ink),
            decorationBox = { innerTextField ->
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(height)
                            .background(colors.paperWhite, LabeledTextFieldShape)
                            .border(OutlineWidth.Regular, colors.ink, LabeledTextFieldShape)
                            .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        BasicText(
                            text = placeholder,
                            modifier = Modifier.fillMaxWidth(),
                            style = textStyle.copy(color = colors.onPaperMuted),
                        )
                    }
                    Box(Modifier.fillMaxWidth(), propagateMinConstraints = true) { innerTextField() }
                }
            },
        )
    }
}

@Preview
@Composable
private fun LabeledTextFieldPreview() =
    ComponentPreview(PreviewSurface.Paper) {
        LabeledTextField(value = "", onValueChange = {}, label = "Your name", placeholder = "What should we call you?")
        LabeledTextField(value = "Alex", onValueChange = {}, label = "Your name", placeholder = "What should we call you?")
        LabeledTextField(
            value = "7F2K",
            onValueChange = {},
            label = "Room code",
            placeholder = "ABCD",
            textStyle = StoryRoundTheme.typography.codeInput.copy(textAlign = TextAlign.Center),
            height = 64.dp,
        )
    }
