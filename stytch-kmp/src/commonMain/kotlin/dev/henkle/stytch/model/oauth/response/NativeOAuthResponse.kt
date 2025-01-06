package dev.henkle.stytch.model.oauth.response

import dev.henkle.stytch.model.session.Session
import dev.henkle.stytch.model.user.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NativeOAuthResponse(
    val session: Session,
    @SerialName(value = "session_jwt")
    val sessionJwt: String,
    @SerialName(value = "session_token")
    val sessionToken: String,
    val user: User,
    @SerialName(value = "user_created")
    val userCreated: Boolean,
)
