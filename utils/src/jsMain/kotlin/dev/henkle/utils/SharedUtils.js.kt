package dev.henkle.utils

actual fun getPlatform(): Platform = Platform.Js

actual fun printToStdErr(msg: String) = console.error(msg)
