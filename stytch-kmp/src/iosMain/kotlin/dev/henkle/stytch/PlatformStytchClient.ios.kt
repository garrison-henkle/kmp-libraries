package dev.henkle.stytch

import dev.henkle.stytch.StytchClient.UriParsingResult
import dev.henkle.stytch.StytchClient.UriType
import dev.henkle.stytch.model.sdk.Config
import dev.henkle.stytch.utils.isOAuthCallback

actual class PlatformStytchClient internal constructor(private val config: Config) {
    internal var overrideOAuthCallbackScheme: String? = null
    internal var overrideOAuthCallbackHost: String? = null

    internal actual fun isHandledUri(uri: String): UriType? =
        when {
            uri.isOAuthCallback(
                config = config,
                overrideOAuthCallbackScheme = overrideOAuthCallbackScheme,
                overrideOAuthCallbackHost = overrideOAuthCallbackHost,
            ) -> UriType.OAuth

            else -> null
        }

    internal actual suspend fun handleUri(uri: String, type: UriType): UriParsingResult? =
        when(type) {
            UriType.OAuth -> parseOAuthToken(uri = uri)
        }

    fun configure(
        callbackScheme: String = Config.DEFAULT_CALLBACK_SCHEME,
        callbackHost: String = Config.DEFAULT_CALLBACK_HOST,
    ) {
        this.overrideOAuthCallbackScheme = callbackScheme
        this.overrideOAuthCallbackHost = callbackHost
    }
}
