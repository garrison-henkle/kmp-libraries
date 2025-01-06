package dev.henkle.stytch.model.otp

data class OTPAuthenticateParameters(
    val methodId: String,
    val token: String,
    val sessionDurationMin: UInt?,
)
