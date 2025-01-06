package dev.henkle.stytch.model.session.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SessionAuthenticateRequest(
    @SerialName(value = "session_duration_minutes")
    val sessionDurationMinutes: UInt?
)
