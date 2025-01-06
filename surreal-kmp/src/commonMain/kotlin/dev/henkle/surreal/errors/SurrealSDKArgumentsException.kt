package dev.henkle.surreal.errors

open class SurrealSDKArgumentsException(override val message: String) : SurrealSDKException(message = message)
