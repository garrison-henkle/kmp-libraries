package dev.henkle.stytch.model.session.response

import dev.henkle.stytch.model.StytchResponseData
import kotlinx.serialization.SerialName

data class SessionRevokeResponse(
    @SerialName(value = "status_code")
    override val status: Int,
    @SerialName(value = "request_id")
    override val requestId: String,
) : StytchResponseData
