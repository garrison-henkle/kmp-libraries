package dev.henkle.stytch.model.session

enum class SessionKey(val id: String) {
    PKCE(id = "stytch_pkce"),
    SessionJWT(id = "stytch_session_jwt"),
    SessionOpaque(id = "stytch_session_token"),
}
