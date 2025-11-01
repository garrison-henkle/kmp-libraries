package dev.henkle.utils

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.fputs
import platform.posix.stderr

@OptIn(ExperimentalForeignApi::class)
actual fun printToStdErr(msg: String) {
    fputs(msg, stderr)
}
