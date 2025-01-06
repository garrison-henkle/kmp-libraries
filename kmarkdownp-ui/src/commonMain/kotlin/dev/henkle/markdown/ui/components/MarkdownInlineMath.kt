package dev.henkle.markdown.ui.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Density
import dev.henkle.markdown.ui.model.InlineUIElement
import dev.henkle.markdown.ui.model.style.InlineMathStyle
import dev.henkle.markdown.ui.utils.LocalMarkdownStyle

@Composable
fun MarkdownInlineMath(element: InlineUIElement.Math) {
    val style = LocalMarkdownStyle.current.inlineMath
    // TODO(garrison)
    Text(
        text = element.equation,
        style = style.textStyle,
    )
}

fun Density.createInlineMathPlaceholder(
    element: InlineUIElement.Math,
    measurer: TextMeasurer,
    style: InlineMathStyle,
): Placeholder {
    val measureResult = measurer.measure(
        text = element.equation,
        style = style.textStyle,
    )
    return Placeholder(
        width = measureResult.size.width.toSp(),
        height = measureResult.size.height.toSp(),
        placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
    )
}
