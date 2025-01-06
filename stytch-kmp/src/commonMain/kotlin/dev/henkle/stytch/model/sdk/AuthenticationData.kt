package dev.henkle.stytch.model.sdk

import dev.henkle.stytch.model.session.Session
import dev.henkle.stytch.model.user.User

data class AuthenticationData(
    val token: String,
    val jwt: String,
    val user: User,
    val session: Session?,
)
