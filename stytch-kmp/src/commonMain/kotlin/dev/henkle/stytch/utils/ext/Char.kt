package dev.henkle.stytch.utils.ext

import kotlin.experimental.ExperimentalNativeApi

private const val MIN_SUPPLEMENTARY_CODE_POINT = 0x010000
private const val MAX_CODE_POINT = 0X10FFFF

fun Char.Companion.isBmpCodePoint(codePoint: Int): Boolean =
    codePoint ushr 16 == 0

@ExperimentalNativeApi
fun Char.Companion.isSupplementaryCodePoint(codePoint: Int): Boolean =
    codePoint >= MIN_SUPPLEMENTARY_CODE_POINT && codePoint < (MAX_CODE_POINT + 1)

@ExperimentalNativeApi
fun Char.Companion.highSurrogate(codePoint: Int) = Char(
    (codePoint ushr 10) + (MIN_HIGH_SURROGATE.code - (MIN_SUPPLEMENTARY_CODE_POINT ushr 10)),
)

fun Char.Companion.lowSurrogate(codePoint: Int) = Char(
    (codePoint and 0x3ff) + MIN_LOW_SURROGATE.code,
)