package com.example.storyround.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.storyround.presentation.model.StorySegmentUi
import com.example.storyround.ui.preview.ComponentPreview
import com.example.storyround.ui.preview.PreviewPlayers
import com.example.storyround.ui.preview.PreviewStory
import com.example.storyround.ui.theme.OutlineWidth
import com.example.storyround.ui.theme.StoryRoundTheme
import com.example.storyround.ui.theme.toColor
import com.example.storyround.ui.theme.toContentColor
import kotlinx.coroutines.delay

private const val HIGHLIGHT_ALPHA = 0.17f
private const val NEWEST_HIGHLIGHT_ALPHA = 0.36f
private const val FADE_IN_MILLIS = 600
private const val SCROLL_DELAY_MILLIS = 250L
private const val NEWEST_TAG_ID = "newestTag"
private const val NO_NEWEST_PIECE = -1
private const val HIGHLIGHT_ASCENT_EM = 0.92f
private const val HIGHLIGHT_DESCENT_EM = 0.28f
private val HighlightPaddingX = 3.dp
private val HighlightPaddingY = 2.dp
private val HighlightRadius = 4.dp
private val HighlightUnderline = 3.dp
private val PieceGap = 14.sp

private val TagShape = RoundedCornerShape(6.dp)
private val TagGap = 6.dp
private val TagPaddingX = 6.dp
private val TagPaddingY = 1.5.dp
private val TagOutline = OutlineWidth.Fine
private val TagHeight = 21.dp

private val StoryTextPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 28.dp)

@Composable
internal fun StoryText(
    segments: List<StorySegmentUi>,
    modifier: Modifier = Modifier,
    newestTag: String? = null,
    contentPadding: PaddingValues = StoryTextPadding,
) {
    val emphasiseNewest = newestTag != null
    val scroll = rememberScrollState()
    val newestPieceAlpha = rememberNewestPieceFadeIn(pieceCount = segments.size, enabled = emphasiseNewest)
    ScrollToNewestPieceEffect(scroll, pieceCount = segments.size, enabled = emphasiseNewest)

    val colors = StoryRoundTheme.colors
    val style = StoryRoundTheme.typography.story.copy(color = colors.ink)
    val newestIndex = if (emphasiseNewest) segments.lastIndex else NO_NEWEST_PIECE
    val story =
        remember(segments, newestIndex, newestPieceAlpha.value) {
            buildStoryText(segments, newestIndex, colors.ink.copy(alpha = newestPieceAlpha.value))
        }
    val tagContent = newestTag?.let { tag -> segments.lastOrNull()?.let { rememberNewestTagContent(tag, it) } }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    BasicText(
        text = story.text,
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(contentPadding)
                .semantics { contentDescription = segments.joinToString(" ") { it.text } }
                .pieceHighlights(
                    layout = { layout },
                    story = story,
                    segments = segments,
                    newestIndex = newestIndex,
                    newestPieceAlpha = { newestPieceAlpha.value },
                    fontSize = style.fontSize,
                ),
        style = style,
        onTextLayout = { layout = it },
        inlineContent = tagContent?.let { mapOf(NEWEST_TAG_ID to it) } ?: emptyMap(),
    )
}

@Composable
private fun rememberNewestPieceFadeIn(
    pieceCount: Int,
    enabled: Boolean,
): Animatable<Float, AnimationVector1D> {
    val alpha = remember { Animatable(1f) }
    LaunchedEffect(pieceCount) {
        if (enabled && pieceCount > 0) {
            alpha.snapTo(0f)
            alpha.animateTo(1f, tween(FADE_IN_MILLIS))
        }
    }
    return alpha
}

@Composable
private fun ScrollToNewestPieceEffect(
    scroll: ScrollState,
    pieceCount: Int,
    enabled: Boolean,
) {
    LaunchedEffect(pieceCount) {
        if (enabled) {
            delay(SCROLL_DELAY_MILLIS)
            scroll.animateScrollTo(scroll.maxValue)
        }
    }
}

private class AnnotatedStory(
    val text: AnnotatedString,
    val ranges: List<TextRange>,
)

private fun buildStoryText(
    segments: List<StorySegmentUi>,
    newestIndex: Int,
    newestInk: Color,
): AnnotatedStory {
    val ranges = mutableListOf<TextRange>()
    val text =
        buildAnnotatedString {
            segments.forEachIndexed { index, segment ->
                if (index > 0) withStyle(SpanStyle(letterSpacing = PieceGap)) { append(' ') }
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
private fun rememberNewestTagContent(
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
                    width = (textSize.width.toDp() + TagPaddingX * 2 + TagOutline * 2 + TagGap).toSp(),
                    height = TagHeight.toSp(),
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
                        .background(author?.colour?.toColor() ?: colors.ink, TagShape)
                        .border(TagOutline, colors.ink, TagShape)
                        .padding(horizontal = TagPaddingX, vertical = TagPaddingY),
                style = tagStyle.copy(color = author?.colour?.toContentColor() ?: colors.paper),
            )
        }
    }
}

private fun Modifier.pieceHighlights(
    layout: () -> TextLayoutResult?,
    story: AnnotatedStory,
    segments: List<StorySegmentUi>,
    newestIndex: Int,
    newestPieceAlpha: () -> Float,
    fontSize: TextUnit,
): Modifier =
    drawBehind {
        val result = layout() ?: return@drawBehind
        val fontSizePx = fontSize.toPx()
        story.ranges.forEachIndexed { index, range ->
            val author = segments[index].author ?: return@forEachIndexed
            val isNewest = index == newestIndex
            val fadeIn = if (isNewest) newestPieceAlpha() else 1f
            val washAlpha = if (isNewest) NEWEST_HIGHLIGHT_ALPHA else HIGHLIGHT_ALPHA
            val color = author.colour.toColor()
            drawHighlight(result, range, fontSizePx, wash = color.copy(alpha = washAlpha * fadeIn), underline = color.copy(alpha = fadeIn))
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
    val underlineHeight = HighlightUnderline.toPx()
    val firstLine = layout.getLineForOffset(range.start)
    val lastLine = layout.getLineForOffset(range.end - 1)
    for (line in firstLine..lastLine) {
        val box = lineHighlightBox(layout, range, line, isFirstLine = line == firstLine, isLastLine = line == lastLine, fontSizePx)
        clipPath(Path().apply { addRoundRect(box) }) {
            drawRect(wash, topLeft = Offset(box.left, box.top), size = Size(box.width, box.height))
            drawRect(
                underline,
                topLeft = Offset(box.left, box.bottom - underlineHeight),
                size = Size(box.width, underlineHeight),
            )
        }
    }
}

private fun DrawScope.lineHighlightBox(
    layout: TextLayoutResult,
    range: TextRange,
    line: Int,
    isFirstLine: Boolean,
    isLastLine: Boolean,
    fontSizePx: Float,
): RoundRect {
    val padX = HighlightPaddingX.toPx()
    val padY = HighlightPaddingY.toPx()
    val radius = CornerRadius(HighlightRadius.toPx())
    val left = if (isFirstLine) layout.getHorizontalPosition(range.start, true) else layout.getLineLeft(line)
    val right = if (isLastLine) layout.getHorizontalPosition(range.end, true) else layout.getLineRight(line)
    val baseline = layout.getLineBaseline(line)
    val startRadius = if (isFirstLine) radius else CornerRadius.Zero
    val endRadius = if (isLastLine) radius else CornerRadius.Zero
    return RoundRect(
        left = left - padX,
        top = baseline - fontSizePx * HIGHLIGHT_ASCENT_EM - padY,
        right = right + padX,
        bottom = baseline + fontSizePx * HIGHLIGHT_DESCENT_EM + padY,
        topLeftCornerRadius = startRadius,
        topRightCornerRadius = endRadius,
        bottomRightCornerRadius = endRadius,
        bottomLeftCornerRadius = startRadius,
    )
}

@Preview
@Composable
private fun StoryTextPreview() =
    ComponentPreview {
        StoryPaper(Modifier.fillMaxWidth().height(360.dp)) { StoryText(PreviewStory.segments) }
    }

@Preview
@Composable
private fun StoryTextNewestPiecePreview() =
    ComponentPreview {
        StoryPaper(Modifier.fillMaxWidth().height(360.dp)) {
            StoryText(PreviewStory.segments, newestTag = PreviewPlayers.jordan.name.uppercase())
        }
    }
