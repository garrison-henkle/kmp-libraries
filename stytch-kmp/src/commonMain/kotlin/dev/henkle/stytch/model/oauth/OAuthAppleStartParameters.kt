package dev.henkle.stytch.model.oauth

import kotlinx.serialization.Serializable

@Serializable
data class OAuthAppleStartParameters(
    val customScopes: List<String>?,
    val loginRedirectUrl: String?,
    val signupRedirectUrl: String?,
    val iOSSessionDurationMin: UInt?,
)
