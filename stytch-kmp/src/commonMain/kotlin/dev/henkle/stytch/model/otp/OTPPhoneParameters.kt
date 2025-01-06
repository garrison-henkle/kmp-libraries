package dev.henkle.stytch.model.otp

data class OTPPhoneParameters(
    val phoneNumber: String,
    val expirationMin: UInt,
)
