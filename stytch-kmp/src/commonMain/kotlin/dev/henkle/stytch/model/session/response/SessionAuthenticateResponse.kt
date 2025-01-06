package dev.henkle.stytch.model.session.response

import dev.henkle.stytch.model.StytchAuthResponseData
import dev.henkle.stytch.model.session.Session
import dev.henkle.stytch.model.user.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SessionAuthenticateResponse(
    @SerialName(value = "status_code")
    override val status: Int,
    @SerialName(value = "request_id")
    override val requestId: String,
    @SerialName(value = "user_id")
    override val userId: String,
    override val user: User,
    @SerialName(value = "session_jwt")
    override val sessionJwt: String,
    @SerialName(value = "session_token")
    override val sessionToken: String,
    override val session: Session?
) : StytchAuthResponseData
