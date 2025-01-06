package dev.henkle.test

actual fun printToStdErr(msg: String) = console.error(msg)
