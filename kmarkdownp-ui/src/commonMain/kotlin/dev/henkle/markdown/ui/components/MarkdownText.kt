package dev.henkle.markdown.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.util.fastForEach
import co.touchlab.kermit.Logger
import dev.henkle.markdown.ui.model.UIElement
import dev.henkle.markdown.ui.utils.LocalMarkdownInlineContent
import dev.henkle.markdown.ui.utils.LocalMarkdownStyle
import dev.henkle.markdown.ui.utils.Renderer
import dev.henkle.markdown.ui.utils.getPlatformTextStyle
import dev.henkle.markdown.ui.utils.getRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

@Composable
fun MarkdownText(
    modifier: Modifier = Modifier,
    element: UIElement.Text,
) {
    val style = LocalMarkdownStyle.current.text
    val inlineContent = LocalMarkdownInlineContent.current
    val renderer = remember { getRenderer() }
    when (renderer) {
        Renderer.AndroidCanvas -> {
            DynamicLineHeightText(
                modifier = modifier,
                text = element.text,
                inlineContent = inlineContent,
                style = style,
            )
        }
        Renderer.Skiko -> {
            BasicText(
                modifier = modifier,
                text = element.text,
                inlineContent = inlineContent,
                style = style,
            )
        }
    }

}

private data class Line(val text: AnnotatedString, val style: TextStyle)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DynamicLineHeightText(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    style: TextStyle = TextStyle.Default,
    inlineContent: Map<String, InlineTextContent>,
) {
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    var lines: List<Line> by remember { mutableStateOf(emptyList<Line>()) }
    val text: AnnotatedString by rememberUpdatedState(newValue = text)
    val style: TextStyle by rememberUpdatedState(newValue = style)
    val inlineTextContent: Map<String, InlineTextContent> by rememberUpdatedState(newValue = inlineContent)
    var placeholders: List<PlaceholderRange> by remember {
        mutableStateOf(value = text.resolveInlineContent(inlineContent = inlineTextContent).first)
    }

    LaunchedEffect(Unit) {
        snapshotFlow { text to inlineTextContent }
            .collectLatest { (text, inlineContent) ->
                placeholders = withContext(Dispatchers.Default) {
                    text.resolveInlineContent(inlineContent = inlineContent).first
                }
            }
    }

    BoxWithConstraints(modifier = modifier) {
        LaunchedEffect(density, maxWidth) {
            val maxWidthPx = with(density) { maxWidth.roundToPx() }
            snapshotFlow { Triple(text, style, placeholders) }
                .collectLatest { (text, style, placeholders) ->
                    val measureResult = measurer.measure(
                        text = text,
                        style = style,
                        placeholders = placeholders,
                        constraints = Constraints(maxWidth = maxWidthPx),
                    )
                    val computedLines = mutableListOf<Line>()
                    for (line in 0..<measureResult.lineCount) {
                        val start = measureResult.getLineStart(lineIndex = line)
                        val end = measureResult.getLineEnd(lineIndex = line)
                        if (start >= end) continue
                        val lineHeightPx = measureResult.multiParagraph.getLineHeight(lineIndex = line)
                        val lineHeight = with(density) { lineHeightPx.toSp() }
                        val trimmedEnd = if (text.isNotEmpty() && text.getOrNull(index = end - 1) == '\n') end - 1 else end
                        computedLines += Line(
                            text =  text.subSequence(startIndex = start, endIndex = trimmedEnd),
                            style = style.copy(lineHeight = lineHeight.coerceAtLeast(minimumValue = style.lineHeight)),
                        )
                    }
                    lines = computedLines
                }
        }

        Column(modifier = Modifier.width(intrinsicSize = IntrinsicSize.Max)) {
            lines.forEachIndexed { i, line ->
                Text(
                    modifier = Modifier,
                    text = line.text,
                    style = line.style.copy(
                        lineHeightStyle = LineHeightStyle(
                            trim = when {
                                i == 0 && lines.size != 1 -> LineHeightStyle.Trim.LastLineBottom
                                i != lines.lastIndex && lines.size != 1 -> LineHeightStyle.Trim.FirstLineTop
                                else -> LineHeightStyle.Trim.Both
                            },
                            alignment = LineHeightStyle.Alignment.Proportional,
                        ),
                        platformStyle = getPlatformTextStyle(),
                    ),
                    inlineContent = inlineTextContent,
                    lineHeight = line.style.lineHeight,
                )
            }
        }
    }
}

private fun TextUnit.coerceAtLeast(minimumValue: TextUnit): TextUnit =
    when {
        (isEm && minimumValue.isEm) || (isSp && minimumValue.isSp) -> {
            if (this.value < minimumValue.value) minimumValue else this
        }
        isUnspecified || minimumValue.isUnspecified -> TextUnit.Unspecified
        else -> {
            Logger.e("KMarkdownP UI Demo") {
                "coerceAtLeast was used incorrectly! The receiver and minimumValue parameter must have matching units!"
            }
            TextUnit.Unspecified
        }
    }

// Below code taken from AnnotatedStringResolveInlineContent.kt in the Jetpack Compose source

private typealias PlaceholderRange = AnnotatedString.Range<Placeholder>
private typealias InlineContentRange = AnnotatedString.Range<@Composable (alternateText: String) -> Unit>

/**
 * Attempts to match AnnotatedString placeholders with passed [InlineTextContent]
 *
 * Matches will produce a entry in both returned lists.
 *
 * Non-matches will be ignored silently.
 */
private fun AnnotatedString.resolveInlineContent(
    inlineContent: Map<String, InlineTextContent>?
): Pair<List<PlaceholderRange>, List<InlineContentRange>> {
    if (inlineContent.isNullOrEmpty()) {
        return EmptyInlineContent
    }
    val inlineContentAnnotations = getStringAnnotations(INLINE_CONTENT_TAG, 0, text.length)

    val placeholders = mutableListOf<AnnotatedString.Range<Placeholder>>()
    val inlineComposables = mutableListOf<AnnotatedString.Range<@Composable (String) -> Unit>>()
    inlineContentAnnotations.fastForEach { annotation ->
        inlineContent[annotation.item]?.let { inlineTextContent ->
            placeholders.add(
                AnnotatedString.Range(
                    inlineTextContent.placeholder,
                    annotation.start,
                    annotation.end
                )
            )
            inlineComposables.add(
                InlineContentRange(
                    inlineTextContent.children,
                    annotation.start,
                    annotation.end
                )
            )
        }
    }
    return Pair(placeholders, inlineComposables)
}

private val EmptyInlineContent: Pair<List<PlaceholderRange>, List<InlineContentRange>> =
    Pair(emptyList(), emptyList())

/**
 * The annotation tag used by inline content.
 */
private const val INLINE_CONTENT_TAG = "androidx.compose.foundation.text.inlineContent"
