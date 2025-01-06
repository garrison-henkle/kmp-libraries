package dev.henkle.test

actual fun executeCommand(command: String): ProcessOutput = throw NotImplementedError("Commands are not implemented for web targets!")
