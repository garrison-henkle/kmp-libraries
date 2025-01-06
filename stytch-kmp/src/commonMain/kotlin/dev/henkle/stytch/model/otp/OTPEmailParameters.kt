package dev.henkle.stytch.model.otp

data class OTPEmailParameters(
    val email: String,
    val expirationMin: UInt,
    val loginTemplateId: String? = null,
    val signupTemplateId: String? = null,
)
