package dev.henkle.markdown.ui.model.style

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import dev.henkle.markdown.ui.model.Padding

data class LinkStyle(
    val textStyle: TextStyle,
    val backgroundColor: Color,
    val margin: Padding,
    val padding: Padding,
) : Style
