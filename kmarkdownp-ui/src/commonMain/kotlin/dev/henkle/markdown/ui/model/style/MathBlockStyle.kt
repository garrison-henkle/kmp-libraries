package dev.henkle.markdown.ui.model.style

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

data class MathBlockStyle(
    val textStyle: TextStyle,
    val backgroundColor: Color,
    val verticalPadding: Dp,
    val horizontalPadding: Dp,
) : Style
