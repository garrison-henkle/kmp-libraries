package dev.henkle.stytch.oauth

import dev.henkle.store.Storage
import dev.henkle.stytch.model.StytchAuthResponseData
import dev.henkle.stytch.model.oauth.OAuthAppleStartParameters
import dev.henkle.stytch.model.oauth.OAuthStartParameters
import dev.henkle.stytch.model.sdk.Config
import dev.henkle.stytch.model.user.User
import dev.henkle.stytch.utils.StytchHTTPClient

actual class AppleOAuthProvider internal actual constructor(
    httpClient: StytchHTTPClient,
    storage: Storage,
    private val config: Config,
    onAuthenticate: (StytchAuthResponseData, User.Name?) -> Unit,
    launchBrowser: (url: String) -> Unit,
) {
    private val delegate = OAuthImpl.OAuthProviderImpl(
        provider = OAuthProvider.Apple,
        storage = storage,
        config = config,
        launchBrowser = launchBrowser,
    )

    actual suspend fun start(
        customScopes: List<String>?,
        loginRedirectUrl: String?,
        signupRedirectUrl: String?,
        iOSSessionDurationMin: UInt?
    ) {
        start(
            parameters = OAuthAppleStartParameters(
                customScopes = customScopes,
                loginRedirectUrl = loginRedirectUrl,
                signupRedirectUrl = signupRedirectUrl,
                iOSSessionDurationMin = iOSSessionDurationMin,
            )
        )
    }

    actual suspend fun start(parameters: OAuthAppleStartParameters) =
        delegate.start(
            parameters = OAuthStartParameters(
                publicToken = config.publicToken,
                loginRedirectUrl = parameters.loginRedirectUrl,
                signupRedirectUrl = parameters.signupRedirectUrl,
                customScopes = parameters.customScopes,
            )
        )
}
