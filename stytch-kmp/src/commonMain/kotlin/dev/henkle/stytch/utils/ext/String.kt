package dev.henkle.stytch.utils.ext

val String.isTestToken: Boolean get() = startsWith(prefix = "public-token-test")

fun String.codePointAt(index: Int): Int = this[index].code
