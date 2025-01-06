package dev.henkle.utils

@OptIn(ExperimentalForeignApi::class)
actual fun printToStdErr(msg: String) {
    fputs(msg, stderr)
}
