package dev.henkle.stytch.model.otp.response

import dev.henkle.stytch.model.StytchResponseData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class OTPSendResponseData(
    @SerialName(value = "status_code")
    override val status: Int,
    @SerialName(value = "request_id")
    override val requestId: String,
    @SerialName(value = "method_id")
    val methodId: String,
) : StytchResponseData
