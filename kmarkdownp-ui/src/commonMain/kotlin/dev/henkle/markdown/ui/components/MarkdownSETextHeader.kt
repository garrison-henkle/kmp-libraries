package dev.henkle.markdown.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
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
fun MarkdownSETextHeader(modifier: Modifier = Modifier, element: UIElement.SETextHeader) {
    val style = LocalMarkdownStyle.current
    val inlineContent = LocalMarkdownInlineContent.current
    val headerStyle = remember (style, element) {
        when (element) {
            is UIElement.SETextH1 -> style.seTextH1
            is UIElement.SETextH2 -> style.seTextH2
        }
    }
    val styleForChildren = remember (style, headerStyle) {
        style.copy(
            text = headerStyle.textStyle,
            divider = headerStyle.dividerStyle,
            h1 = headerStyle.textStyle,
            h2 = headerStyle.textStyle,
            h3 = headerStyle.textStyle,
            h4 = headerStyle.textStyle,
            h5 = headerStyle.textStyle,
            h6 = headerStyle.textStyle,
            seTextH1 = headerStyle,
            seTextH2 = headerStyle,
        )
    }
    Column(modifier = modifier) {
        ProvideMarkdownStyle(style = styleForChildren) {
            ProvideMarkdownInlineContent(inlineContent = inlineContent) {
                MarkdownContent(elements = element.elements)
            }
        }

        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = headerStyle.dividerStyle.color,
            thickness = headerStyle.dividerStyle.thickness,
        )
    }
}
