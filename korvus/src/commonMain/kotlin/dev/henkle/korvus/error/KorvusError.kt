package dev.henkle.korvus.error

/**
 * Represents an error thrown by the Korvus SDK.
 */
sealed class KorvusError : Exception() {
    data class SDK(val ex: Exception) : KorvusError()
    data class Raven(val error: RavenError) : KorvusError()
}
