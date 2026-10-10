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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.theme.StoryRoundTheme

private val SrTextFieldShape = RoundedCornerShape(14.dp)

@Composable
internal fun SrTextField(
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
        BasicText(label.uppercase(), style = StoryRoundTheme.typography.label.copy(color = colors.onPaperMuted))
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
                            .background(colors.paperWhite, SrTextFieldShape)
                            .border(2.5.dp, colors.ink, SrTextFieldShape)
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
