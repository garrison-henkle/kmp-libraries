package dev.henkle.stytch.model.otp.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OTPPhoneRequest(
    @SerialName(value = "phone_number")
    val phoneNumber: String,
    @SerialName(value = "expiration_minutes")
    val expirationMin: UInt,
)
