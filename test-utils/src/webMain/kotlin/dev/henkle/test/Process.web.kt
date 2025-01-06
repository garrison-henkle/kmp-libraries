package dev.henkle.test

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

actual class Process actual constructor(command: String) {
    init {
        throw NotImplementedError("Process has not been implemented for web targets!")
    }

    actual val stdout = StringBuilder()
    actual val stderr = StringBuilder()
    actual val output: Flow<String> = emptyFlow()

    actual suspend fun kill() = Unit

    actual suspend fun wait(): ProcessOutput = ProcessOutput(0, "", "")
}
