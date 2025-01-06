package dev.henkle.markdown.ui.components

import androidx.compose.runtime.Composable
import dev.henkle.markdown.ui.model.InlineUIElement
import dev.henkle.markdown.ui.utils.LocalMarkdownUIComponents

@Composable
fun MarkdownInlineContent(element: InlineUIElement) {
    val components = LocalMarkdownUIComponents.current
    when(element) {
        is InlineUIElement.Code -> components.inlineCode(element)
        is InlineUIElement.Math -> components.inlineMath(element)
        is InlineUIElement.Link -> components.inlineLink(element)
    }
}
