package dev.henkle.utils

actual fun getPlatform(): Platform = Platform.Wasm

actual fun printToStdErr(msg: String) = println(message = msg)
