package dev.henkle.stytch.oauth

import dev.henkle.store.Storage
import dev.henkle.stytch.model.StytchAuthResponseData
import dev.henkle.stytch.model.oauth.OAuthAppleStartParameters
import dev.henkle.stytch.model.sdk.Config
import dev.henkle.stytch.model.user.User
import dev.henkle.stytch.utils.StytchHTTPClient

expect class AppleOAuthProvider internal constructor(
    httpClient: StytchHTTPClient,
    storage: Storage,
    config: Config,
    onAuthenticate: (StytchAuthResponseData, User.Name?) -> Unit,
    launchBrowser: (url: String) -> Unit,
) {
    suspend fun start(
        customScopes: List<String>? = null,
        loginRedirectUrl: String? = null,
        signupRedirectUrl: String? = null,
        iOSSessionDurationMin: UInt? = null,
    )

    suspend fun start(parameters: OAuthAppleStartParameters)
}
