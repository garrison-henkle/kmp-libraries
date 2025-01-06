package dev.henkle.markdown.ui.model.style

import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle

data class InlineMathStyle(
    val textStyle: TextStyle,
    override val inlineAlignment: PlaceholderVerticalAlign,
) : InlineStyle
