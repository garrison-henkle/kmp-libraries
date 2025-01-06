package dev.henkle.compose.sheet

sealed interface Anchor {
    data class Fixed(val px: Float) : Anchor
    data class Percentage(val percent: Float) : Anchor
    data object Max : Anchor
    data object Min : Anchor
}
