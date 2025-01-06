package dev.henkle.test

data class ProcessOutput(
    val code: Int,
    val stdout: String,
    val stderr: String,
)
