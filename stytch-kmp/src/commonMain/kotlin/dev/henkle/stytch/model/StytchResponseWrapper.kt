package dev.henkle.stytch.model

import kotlinx.serialization.Serializable

@Serializable
data class StytchResponseWrapper<T>(val data: T)
