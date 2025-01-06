package dev.henkle.markdown.ui.model.style

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle

data class InlineCodeStyle(
    val textStyle: TextStyle,
    val border: BorderStroke?,
    val borderColor: Color,
    override val inlineAlignment: PlaceholderVerticalAlign,
) : InlineStyle
