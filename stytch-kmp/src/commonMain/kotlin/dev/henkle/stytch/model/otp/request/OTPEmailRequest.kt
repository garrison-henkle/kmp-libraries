package dev.henkle.stytch.model.otp.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OTPEmailRequest(
    val email: String,
    @SerialName(value = "expiration_minutes")
    val expirationMin: UInt,
    @SerialName(value = "login_template_id")
    val loginTemplateId: String? = null,
    @SerialName(value = "signup_template_id")
    val signupTemplateId: String? = null,
)
