package dev.henkle.surreal.errors

open class SurrealSDKResultException(override val message: String) : SurrealSDKException(message = message)
