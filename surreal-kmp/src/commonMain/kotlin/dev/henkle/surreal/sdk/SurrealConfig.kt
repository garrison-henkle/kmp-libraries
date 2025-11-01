package dev.henkle.surreal.sdk

data class SurrealConfig(
    var logLevel: LogLevel = LogLevel.Error,
) {
    enum class LogLevel {
        Error,
        Warn,
        Info,
        Debug,
    }
}
