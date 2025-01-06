package dev.henkle.utils

actual fun getPlatform(): Platform = Platform.Jvm

actual fun printToStdErr(msg: String) = System.err.print(msg)
