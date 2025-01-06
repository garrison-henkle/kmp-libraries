package dev.henkle.compose.paging

data class TransformedItem<O, T>(
    val item: T,
    val original: O? = null,
)