package dev.henkle.markdown.ui.model.style

import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

data class LinkDefinitionStyle(
    val linkStyle: LinkStyle,
    val contentStyle: TextStyle,
    val rowAlignment: Alignment.Vertical,
    val spacing: Dp,
) : Style
