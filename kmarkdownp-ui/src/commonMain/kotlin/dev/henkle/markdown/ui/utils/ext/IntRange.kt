package dev.henkle.markdown.ui.utils.ext

fun IntRange.Companion.of(value: Int): IntRange =
    IntRange(start = value, endInclusive = value + 1)
