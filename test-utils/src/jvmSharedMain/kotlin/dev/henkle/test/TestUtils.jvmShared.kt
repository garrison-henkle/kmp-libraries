package dev.henkle.test

import java.io.File
import java.lang.ProcessBuilder

actual fun executeCommand(command: String): ProcessOutput {
    val process = ProcessBuilder()
        .command("/bin/bash", "-c", command)
        .start()
    return ProcessOutput(
        code = process.waitFor(),
        stdout = process.inputStream.use { stream ->
            stream.bufferedReader().use { reader ->
                reader.readText()
            }
        },
        stderr = process.errorStream.use { stream ->
            stream.bufferedReader().use { reader ->
                reader.readText()
            }
        },
    )
}
