package dev.henkle.markdown.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import dev.henkle.markdown.ui.components.shared.MarkdownLink
import dev.henkle.markdown.ui.model.InlineUIElement
import dev.henkle.markdown.ui.model.style.LinkStyle
import dev.henkle.markdown.ui.utils.LocalMarkdownStyle
import dev.henkle.markdown.ui.utils.ext.getText
import kotlin.math.max

@Composable
fun MarkdownInlineLink(modifier: Modifier = Modifier, element: InlineUIElement.Link) {
    val style = LocalMarkdownStyle.current.inlineLink
    MarkdownLink(
        modifier = modifier,
        title = element.title,
        label = element.label,
        labelRaw = element.labelRaw,
        style = style.linkStyle,
    )
}

fun Density.createInlineLinkPlaceholder(
    element: InlineUIElement.Link,
    measurer: TextMeasurer,
    style: LinkStyle,
): Placeholder {
    val textSize = measurer.measure(text = (element.title ?: element.label).getText(), style = style.textStyle).size
    val linkWidth = textSize.width.toSp().value + (style.padding.start + style.padding.end).toSp().value
    val linkHeight = textSize.height.toSp().value + (style.padding.top + style.padding.bottom).toSp().value
    val width = (max(linkWidth, linkHeight) + (style.margin.start + style.margin.end).toSp().value).sp
    val height = (linkHeight + (style.margin.top + style.margin.bottom).toSp().value).sp
    return Placeholder(
        width = width,
        height = height,
        placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
    )
}
