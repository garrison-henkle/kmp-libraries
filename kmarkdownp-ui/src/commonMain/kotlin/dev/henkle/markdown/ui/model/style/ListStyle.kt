package dev.henkle.markdown.ui.model.style

import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

data class ListStyle(
    val contentAlignment: Alignment.Vertical,
    val markerStyle: TextStyle,
    val markerStartMargin: Dp,
    val markerEndMargin: Dp,
    val markerAlignment: Alignment.Vertical,
    val levelIndentation: Dp,
    val bulletChar: Char,
    val numberChar: Char,
    val itemSpacing: Dp,
) : Style
