package dev.henkle.utils

actual fun getPlatform(): Platform = Platform.Android

actual fun printToStdErr(msg: String) = System.err.print(msg)
