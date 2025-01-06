package dev.henkle.markdown.ui.model.style

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

data class BlockquoteStyle(
    val textStyle: TextStyle,
    val quoteBarStartMargin: Dp,
    val quoteBarEndMargin: Dp,
    val quoteBarWidth: Dp,
    val quoteBarShape: Shape,
    val quoteBarColor: Color,
) : Style
