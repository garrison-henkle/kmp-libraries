package dev.henkle.markdown.ui.model

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// PaddingValues but made by a sane person
data class Padding(
    val top: Dp = 0.dp,
    val bottom: Dp = 0.dp,
    val start: Dp = 0.dp,
    val end: Dp = 0.dp,
) {
    constructor(
        horizontal: Dp = 0.dp,
        vertical: Dp = 0.dp,
    ) : this(
        top = vertical,
        bottom = vertical,
        start = horizontal,
        end = horizontal,
    )

    constructor(all: Dp = 0.dp) : this(
        top = all,
        bottom = all,
        start = all,
        end = all,
    )
}
