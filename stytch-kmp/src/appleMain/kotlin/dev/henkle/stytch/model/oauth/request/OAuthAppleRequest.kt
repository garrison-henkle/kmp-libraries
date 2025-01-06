package dev.henkle.stytch.model.oauth.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OAuthAppleRequest(
    @SerialName(value = "id_token")
    val idToken: String,
    val nonce: String,
    @SerialName(value = "session_duration_minutes")
    val sessionDurationMin: UInt?,
)
