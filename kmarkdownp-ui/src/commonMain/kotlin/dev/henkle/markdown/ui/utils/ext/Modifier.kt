package dev.henkle.markdown.ui.utils.ext

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import dev.henkle.markdown.ui.model.Padding

fun Modifier.padding(values: Padding): Modifier = padding(
    top = values.top,
    bottom = values.bottom,
    start = values.start,
    end = values.end,
)
