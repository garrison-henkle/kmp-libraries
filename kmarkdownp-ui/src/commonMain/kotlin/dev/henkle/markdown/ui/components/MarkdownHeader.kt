package dev.henkle.markdown.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.henkle.markdown.ui.MarkdownContent
import dev.henkle.markdown.ui.model.UIElement
import dev.henkle.markdown.ui.utils.LocalMarkdownInlineContent
import dev.henkle.markdown.ui.utils.LocalMarkdownStyle
import dev.henkle.markdown.ui.utils.ProvideMarkdownInlineContent
import dev.henkle.markdown.ui.utils.ProvideMarkdownStyle

@Composable
fun MarkdownHeader(modifier: Modifier = Modifier, element: UIElement.Header) {
    val style = LocalMarkdownStyle.current
    val inlineContent = LocalMarkdownInlineContent.current
    val headerStyle = when (element) {
        is UIElement.H1 -> style.h1
        is UIElement.H2 -> style.h2
        is UIElement.H3 -> style.h3
        is UIElement.H4 -> style.h4
        is UIElement.H5 -> style.h5
        is UIElement.H6 -> style.h6
    }
    val styleForChildren = remember (style, headerStyle) {
        val seTextH1Style = style.seTextH1.copy(textStyle = headerStyle)
        val seTextH2Style = style.seTextH2.copy(textStyle = headerStyle)
        style.copy(
            text = headerStyle,
            h1 = headerStyle,
            h2 = headerStyle,
            h3 = headerStyle,
            h4 = headerStyle,
            h5 = headerStyle,
            h6 = headerStyle,
            seTextH1 = seTextH1Style,
            seTextH2 = seTextH2Style,
        )
    }
    ProvideMarkdownStyle(style = styleForChildren) {
        ProvideMarkdownInlineContent(inlineContent = inlineContent) {
            MarkdownContent(elements = element.elements)
        }
    }
}
