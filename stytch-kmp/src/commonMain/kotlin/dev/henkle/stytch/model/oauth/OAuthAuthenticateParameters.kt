package dev.henkle.stytch.model.oauth

data class OAuthAuthenticateParameters(
    val token: String,
    val sessionDurationMin: UInt?,
)
