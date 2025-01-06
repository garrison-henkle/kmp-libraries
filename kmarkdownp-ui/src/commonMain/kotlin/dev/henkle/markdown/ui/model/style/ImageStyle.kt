package dev.henkle.markdown.ui.model.style

import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp

/**
 * For width or height, [Dp.Infinity] fills width/height, [Dp.Unspecified] wraps width/height, other
 * values set constant value
 */
data class ImageStyle(
    val width: Dp = Dp.Infinity,
    val height: Dp = Dp.Unspecified,
    val contentScale: ContentScale,
    val alignment: Alignment,
) : Style
