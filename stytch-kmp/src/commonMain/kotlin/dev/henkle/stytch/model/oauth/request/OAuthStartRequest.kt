package dev.henkle.stytch.model.oauth.request

import dev.henkle.stytch.utils.UrlEncoding

data class OAuthStartRequest(
    val codeChallenge: String,
    val publicToken: String,
    val loginRedirectUrl: String?,
    val signupRedirectUrl: String?,
    val customScopes: List<String>?,
) {
    val queryString: String = mutableMapOf(
        "code_challenge" to codeChallenge,
        "public_token" to publicToken,
        "login_redirect_url" to loginRedirectUrl?.let { UrlEncoding.encode(src = it) },
        "signup_redirect_url" to signupRedirectUrl?.let { UrlEncoding.encode(src = it) },
    ).apply {
        if (customScopes?.isNotEmpty() == true) {
            this["custom_scopes"] =
                UrlEncoding.encode(src = customScopes.joinToString(separator = " "))
        }
    }.map { (key, value) -> "$key=$value" }
        .joinToString(separator = "&")
}
