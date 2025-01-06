package dev.henkle.stytch.model.otp.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OTPAuthenticateRequest(
    @SerialName(value = "method_id")
    val methodId: String,
    val token: String,
    @SerialName(value = "session_duration_minutes")
    val sessionDurationMin: UInt?,
)
