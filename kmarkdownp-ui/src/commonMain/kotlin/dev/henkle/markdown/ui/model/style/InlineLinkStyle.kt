package dev.henkle.markdown.ui.model.style

import androidx.compose.ui.text.PlaceholderVerticalAlign

data class InlineLinkStyle(
    val linkStyle: LinkStyle,
    override val inlineAlignment: PlaceholderVerticalAlign,
) : InlineStyle
