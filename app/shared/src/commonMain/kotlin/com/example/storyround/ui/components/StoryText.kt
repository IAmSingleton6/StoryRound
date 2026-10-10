package com.example.storyround.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.storyround.presentation.model.StorySegmentUi
import com.example.storyround.ui.theme.StoryRoundTheme
import com.example.storyround.ui.theme.toOnUiColor
import com.example.storyround.ui.theme.toUiColor
import kotlinx.coroutines.delay

private const val HIGHLIGHT_ALPHA = 0.17f
private const val NEWEST_HIGHLIGHT_ALPHA = 0.36f
private const val FADE_IN_MILLIS = 600
private const val SCROLL_DELAY_MILLIS = 250L
private const val NEWEST_TAG_ID = "newestTag"

private const val HIGHLIGHT_ASCENT_EM = 0.92f
private const val HIGHLIGHT_DESCENT_EM = 0.28f
private val HighlightPaddingX = 3.dp
private val HighlightPaddingY = 2.dp
private val HighlightRadius = 4.dp
private val HighlightUnderline = 3.dp
private val TagShape = RoundedCornerShape(6.dp)
private val TagGap = 6.dp

@Composable
internal fun StoryText(
    segments: List<StorySegmentUi>,
    modifier: Modifier = Modifier,
    newestTag: String? = null,
) {
    val scroll = rememberScrollState()
    val newestAlpha = remember { Animatable(1f) }
    LaunchedEffect(segments.size) {
        if (newestTag != null && segments.isNotEmpty()) {
            newestAlpha.snapTo(0f)
            newestAlpha.animateTo(1f, tween(FADE_IN_MILLIS))
        }
    }
    LaunchedEffect(segments.size) {
        delay(SCROLL_DELAY_MILLIS)
        scroll.animateScrollTo(scroll.maxValue)
    }
    val colors = StoryRoundTheme.colors
    val style = StoryRoundTheme.typography.story.copy(color = colors.ink)
    val newestIndex = if (newestTag != null) segments.lastIndex else -1
    val story =
        remember(segments, newestIndex, newestAlpha.value) {
            annotate(segments, newestIndex, colors.ink.copy(alpha = newestAlpha.value))
        }
    val tagContent = newestTag?.let { tag -> segments.lastOrNull()?.let { rememberNewestTag(tag, it) } }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val density = LocalDensity.current
    BasicText(
        text = story.text,
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 28.dp)
                .semantics { contentDescription = segments.joinToString(" ") { it.text } }
                .drawBehind {
                    val result = layout ?: return@drawBehind
                    val fontSizePx = with(density) { style.fontSize.toPx() }
                    story.ranges.forEachIndexed { index, range ->
                        val author = segments[index].author ?: return@forEachIndexed
                        val appear = if (index == newestIndex) newestAlpha.value else 1f
                        val wash = if (index == newestIndex) NEWEST_HIGHLIGHT_ALPHA else HIGHLIGHT_ALPHA
                        val colour = author.colour.toUiColor()
                        drawHighlight(result, range, fontSizePx, colour.copy(alpha = wash * appear), colour.copy(alpha = appear))
                    }
                },
        style = style,
        onTextLayout = { layout = it },
        inlineContent = tagContent?.let { mapOf(NEWEST_TAG_ID to it) } ?: emptyMap(),
    )
}

private class AnnotatedStory(
    val text: AnnotatedString,
    val ranges: List<TextRange>,
)

private fun annotate(
    segments: List<StorySegmentUi>,
    newestIndex: Int,
    newestInk: Color,
): AnnotatedStory {
    val ranges = mutableListOf<TextRange>()
    val text =
        buildAnnotatedString {
            segments.forEachIndexed { index, segment ->
                if (index > 0) withStyle(SpanStyle(letterSpacing = 5.sp)) { append(' ') }
                val start = length
                if (index == newestIndex) {
                    withStyle(SpanStyle(color = newestInk)) {
                        append(segment.text)
                    }
                    ranges += TextRange(start, length)
                    appendInlineContent(NEWEST_TAG_ID, segment.author?.name.orEmpty())
                } else {
                    append(segment.text)
                    ranges += TextRange(start, length)
                }
            }
        }
    return AnnotatedStory(text, ranges)
}

@Composable
private fun rememberNewestTag(
    tag: String,
    segment: StorySegmentUi,
): InlineTextContent {
    val colors = StoryRoundTheme.colors
    val tagStyle = StoryRoundTheme.typography.tag
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(tag, segment, tagStyle) {
        val textSize = measurer.measure(tag, tagStyle).size
        val placeholder =
            with(density) {
                Placeholder(
                    width = (textSize.width.toDp() + 12.dp + 3.dp + TagGap).toSp(),
                    height = 21.dp.toSp(),
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                )
            }
        InlineTextContent(placeholder) {
            val author = segment.author
            BasicText(
                text = tag,
                modifier =
                    Modifier
                        .padding(start = TagGap)
                        .wrapContentSize()
                        .background(author?.colour?.toUiColor() ?: colors.ink, TagShape)
                        .border(1.5.dp, colors.ink, TagShape)
                        .padding(horizontal = 6.dp, vertical = 1.5.dp),
                style = tagStyle.copy(color = author?.colour?.toOnUiColor() ?: colors.paper),
            )
        }
    }
}

private fun DrawScope.drawHighlight(
    layout: TextLayoutResult,
    range: TextRange,
    fontSizePx: Float,
    wash: Color,
    underline: Color,
) {
    if (range.collapsed) return
    val padX = HighlightPaddingX.toPx()
    val padY = HighlightPaddingY.toPx()
    val radius = CornerRadius(HighlightRadius.toPx())
    val underlineHeight = HighlightUnderline.toPx()
    val firstLine = layout.getLineForOffset(range.start)
    val lastLine = layout.getLineForOffset(range.end - 1)
    for (line in firstLine..lastLine) {
        val left = if (line == firstLine) layout.getHorizontalPosition(range.start, true) else layout.getLineLeft(line)
        val right = if (line == lastLine) layout.getHorizontalPosition(range.end, true) else layout.getLineRight(line)
        val baseline = layout.getLineBaseline(line)
        val box =
            RoundRect(
                left = left - padX,
                top = baseline - fontSizePx * HIGHLIGHT_ASCENT_EM - padY,
                right = right + padX,
                bottom = baseline + fontSizePx * HIGHLIGHT_DESCENT_EM + padY,
                cornerRadius = radius,
            )
        val clip = Path().apply { addRoundRect(box) }
        clipPath(clip) {
            drawRect(wash, topLeft = Offset(box.left, box.top), size = Size(box.width, box.height))
            drawRect(
                underline,
                topLeft = Offset(box.left, box.bottom - underlineHeight),
                size = Size(box.width, underlineHeight),
            )
        }
    }
}
