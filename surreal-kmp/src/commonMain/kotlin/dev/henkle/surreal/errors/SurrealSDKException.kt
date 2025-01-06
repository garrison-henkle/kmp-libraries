package dev.henkle.surreal.errors

open class SurrealSDKException(
    override val message: String,
    override val cause: Throwable? = null,
) : Exception(message, cause)
