package dev.henkle.markdown.ui.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Density
import dev.henkle.markdown.ui.model.InlineUIElement
import dev.henkle.markdown.ui.model.style.InlineCodeStyle
import dev.henkle.markdown.ui.utils.LocalMarkdownStyle

@Composable
fun MarkdownInlineCode(element: InlineUIElement.Code) {
    val style = LocalMarkdownStyle.current.inlineCode
    // TODO(garrison)
    Text(
        text = element.code,
        style = style.textStyle,
    )
}

fun Density.createInlineCodePlaceholder(
    element: InlineUIElement.Code,
    measurer: TextMeasurer,
    style: InlineCodeStyle,
): Placeholder {
    val measureResult = measurer.measure(
        text = element.code,
        style = style.textStyle,
    )
    return Placeholder(
        width = measureResult.size.width.toSp(),
        height = measureResult.size.height.toSp(),
        placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
    )
}
