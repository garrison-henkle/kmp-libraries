package dev.henkle.stytch.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BasicResponseData(
    @SerialName(value = "status_code")
    override val status: Int,
    @SerialName(value = "request_id")
    override val requestId: String,
) : StytchResponseData
