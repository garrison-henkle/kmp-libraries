package dev.henkle.stytch.model

import dev.henkle.stytch.model.session.Session
import dev.henkle.stytch.model.user.User

interface StytchAuthResponseData : StytchResponseData {
    val userId: String
    val user: User
    val sessionJwt: String
    val sessionToken: String
    val session: Session?
}
