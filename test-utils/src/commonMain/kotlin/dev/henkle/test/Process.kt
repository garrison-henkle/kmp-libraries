package dev.henkle.test

import kotlinx.coroutines.flow.Flow

expect class Process(command: String) {
    val stdout: StringBuilder
    val stderr: StringBuilder
    val output: Flow<String>

    suspend fun kill()

    suspend fun wait(): ProcessOutput
}
