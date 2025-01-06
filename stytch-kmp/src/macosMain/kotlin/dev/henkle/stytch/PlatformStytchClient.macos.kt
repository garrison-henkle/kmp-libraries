package dev.henkle.stytch

import dev.henkle.stytch.StytchClient.UriParsingResult
import dev.henkle.stytch.StytchClient.UriType
import dev.henkle.stytch.model.sdk.Config
import dev.henkle.stytch.utils.isOAuthCallback

actual class PlatformStytchClient internal constructor() {
    internal var oauthCallbackPath: String = Config.DEFAULT_CALLBACK_PATH
    internal var oauthCallbackPort: Int = Config.DEFAULT_CALLBACK_PORT
    internal var oauthCallbackRedirect: String = Config.DEFAULT_CALLBACK_REDIRECT_PATH

    internal actual fun isHandledUri(uri: String): UriType? = when {
        uri.isOAuthCallback(
            oauthCallbackPath = oauthCallbackPath,
            oauthCallbackPort = oauthCallbackPort,
        ) -> UriType.OAuth

        else -> null
    }

    internal actual suspend fun handleUri(uri: String, type: UriType): UriParsingResult? =
        when(type) {
            UriType.OAuth -> parseOAuthToken(uri = uri)
        }

    fun configure(
        oauthCallbackPath: String = Config.DEFAULT_CALLBACK_PATH,
        oauthCallbackPort: Int = Config.DEFAULT_CALLBACK_PORT,
        oauthCallbackRedirectPath: String = Config.DEFAULT_CALLBACK_REDIRECT_PATH,
    ) {
        this.oauthCallbackPath = oauthCallbackPath
        this.oauthCallbackPort = oauthCallbackPort
        this.oauthCallbackRedirect = oauthCallbackRedirectPath
    }
}
