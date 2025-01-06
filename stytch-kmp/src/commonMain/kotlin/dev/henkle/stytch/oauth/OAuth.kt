package dev.henkle.stytch.oauth

import dev.henkle.stytch.model.StytchResult
import dev.henkle.stytch.model.oauth.OAuthAuthenticateParameters
import dev.henkle.stytch.model.oauth.OAuthStartParameters
import dev.henkle.stytch.model.oauth.response.OAuthResponse

interface OAuth {
    val amazon: Provider
    val apple: AppleOAuthProvider
    val bitbucket: Provider
    val coinbase: Provider
    val discord: Provider
    val facebook: Provider
    val figma: Provider
    val github: Provider
    val gitlab: Provider
    val google: Provider
    val googleOneTap: GoogleOneTapProvider
    val linkedin: Provider
    val microsoft: Provider
    val salesforce: Provider
    val slack: Provider
    val snapchat: Provider
    val spotify: Provider
    val tiktok: Provider
    val twitch: Provider
    val twitter: Provider
    val yahoo: Provider

    suspend fun authenticate(
        token: String,
        sessionDurationMin: UInt? = null,
    ): StytchResult<OAuthResponse>

    suspend fun authenticate(
        parameters: OAuthAuthenticateParameters,
    ): StytchResult<OAuthResponse>

    interface Provider {
        val provider: OAuthProvider

        suspend fun start(
            customScopes: List<String>? = null,
            loginRedirectUrl: String? = null,
            signupRedirectUrl: String? = null,
        )

        suspend fun start(parameters: OAuthStartParameters)
    }
}
