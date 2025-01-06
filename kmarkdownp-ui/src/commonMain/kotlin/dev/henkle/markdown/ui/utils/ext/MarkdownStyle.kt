package dev.henkle.markdown.ui.utils.ext

import androidx.compose.ui.text.PlaceholderVerticalAlign
import dev.henkle.markdown.ui.MarkdownStyle
import dev.henkle.markdown.ui.model.InlineUIElement

fun MarkdownStyle.getPlaceholderAlignment(element: InlineUIElement): PlaceholderVerticalAlign =
    when (element) {
        is InlineUIElement.Code -> inlineCode.inlineAlignment
        is InlineUIElement.Link -> inlineLink.inlineAlignment
        is InlineUIElement.Math -> inlineMath.inlineAlignment
    }
