package dev.henkle.stytch.model.oauth

data class OAuthStartParameters(
    val publicToken: String,
    val loginRedirectUrl: String?,
    val signupRedirectUrl: String?,
    val customScopes: List<String>?,
)
