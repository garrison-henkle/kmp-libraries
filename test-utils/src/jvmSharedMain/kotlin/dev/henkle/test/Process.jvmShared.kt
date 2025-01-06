package dev.henkle.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

actual class Process actual constructor(command: String) {
    private val scope = CoroutineScope(Dispatchers.IO)
    actual val stdout = StringBuilder()
    actual val stderr = StringBuilder()
    private val _output = MutableSharedFlow<String>(replay = 20)
    actual val output: Flow<String> = _output
    private val process = ProcessBuilder()
        .command("/bin/bash", "-c", command)
        .start()

    init {
        scope.launch {
            try {
                process.inputStream.use { stream ->
                    stream.bufferedReader().use { reader ->
                        var line = reader.readLine()
                        while(line != null) {
                            stdout.appendLine(line)
                            _output.emit(line)
                            line = reader.readLine()
                        }
                    }
                }
            } catch(_: IOException) {}
        }
        scope.launch {
            try {
                process.errorStream.use { stream ->
                    stream.bufferedReader().use { reader ->
                        var line = reader.readLine()
                        while(line != null) {
                            stderr.appendLine(line)
                            _output.emit(line)
                            line = reader.readLine()
                        }
                    }
                }
            } catch(_: IOException) {}
        }
    }

    actual suspend fun kill() {
        process.destroy()
        withContext(Dispatchers.IO) {
            try {
                process.waitFor()
            } catch(_: Exception) {}
        }
        scope.cancel()
    }

    actual suspend fun wait(): ProcessOutput =
        try {
            val code = withContext(Dispatchers.IO) {
                process.waitFor()
            }
            scope.cancel()
            ProcessOutput(code = code, stdout = stdout.toString(), stderr = stderr.toString())
        } catch (ex: Exception) {
            throw Exception("Unable to execute command! $ex")
        }
}
