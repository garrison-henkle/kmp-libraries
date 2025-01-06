package dev.henkle.markdown.ui.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.henkle.markdown.ui.model.UIElement
import dev.henkle.markdown.ui.utils.LocalMarkdownInlineContent
import dev.henkle.markdown.ui.utils.LocalMarkdownStyle

@Composable
fun MarkdownText(
    modifier: Modifier = Modifier,
    element: UIElement.Text,
) {
    val style = LocalMarkdownStyle.current.text
    val inlineContent = LocalMarkdownInlineContent.current
    BasicText(
        modifier = modifier,
        text = element.text,
        inlineContent = inlineContent,
        style = style,
    )
}
