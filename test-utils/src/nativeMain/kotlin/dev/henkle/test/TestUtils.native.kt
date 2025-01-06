package dev.henkle.test

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import platform.posix.FILE
import platform.posix.NULL
import platform.posix.fgets
import platform.posix.fputs
import platform.posix.pclose
import platform.posix.popen
import platform.posix.printf
import platform.posix.stderr

@OptIn(ExperimentalForeignApi::class)
actual fun printToStdErr(msg: String) {
    fputs(msg, stderr)
}

actual fun executeCommand(command: String): ProcessOutput = execute(command).let { (code, stdout) ->
    ProcessOutput(code = code, stdout = stdout, stderr = "")
}

@OptIn(ExperimentalForeignApi::class)
internal fun execute(command: String): Pair<Int, String> {
    val fp: CPointer<FILE>? = popen(command, "r")
    val buffer = ByteArray(4096)
    val output = StringBuilder()

    return if (fp != null) {
        throw Exception("Unable to execute command!")
    } else {
        var scan = fgets(buffer.refTo(index = 0), buffer.size, fp)
        if (scan != null) {
            while (scan != NULL) {
                output.append(scan!!.toKString())
                println(scan.toKString())
                scan = fgets(buffer.refTo(0), buffer.size, fp)
            }
        }
        pclose(fp) to output.trim().toString()
    }
}
