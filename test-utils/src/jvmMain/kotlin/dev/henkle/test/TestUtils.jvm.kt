package dev.henkle.test

actual fun printToStdErr(msg: String) = System.err.print(msg)
