package dev.henkle.stytch.model.oauth.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OAuthAuthenticateRequest(
    val token: String,
    @SerialName(value = "code_verifier")
    val codeVerifier: String,
    @SerialName(value = "session_duration_minutes")
    val sessionDurationMin: UInt?,
)
