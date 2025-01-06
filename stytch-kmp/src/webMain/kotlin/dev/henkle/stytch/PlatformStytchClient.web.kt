package dev.henkle.stytch

import dev.henkle.stytch.StytchClient.UriParsingResult
import dev.henkle.stytch.StytchClient.UriType
import dev.henkle.stytch.model.sdk.Config

@Suppress("UNUSED")
actual class PlatformStytchClient internal constructor(config: Config) {
    internal var domain: String = "${Config.LOCALHOST}:${Config.DEFAULT_WEB_PORT}"
    internal var oauthCallbackEndpoint: String = Config.DEFAULT_CALLBACK_PATH

    internal actual fun isHandledUri(uri: String): UriType? {
        return when {
            getPath(uri = uri).startsWith(prefix = oauthCallbackEndpoint) -> UriType.OAuth
            else -> null
        }
    }

    internal actual suspend fun handleUri(uri: String, type: UriType): UriParsingResult? =
        when (type) {
            UriType.OAuth -> parseOAuthToken(uri = uri)
        }

    fun configure(
        domain: String = "${Config.LOCALHOST}:${Config.DEFAULT_WEB_PORT}",
        oauthCallbackEndpoint: String = Config.DEFAULT_CALLBACK_PATH,
    ) {
        this.domain = domain
        this.oauthCallbackEndpoint = oauthCallbackEndpoint
    }

    private fun getPath(uri: String): String {
        var slashCount = 0
        var i = 0
        for (char in uri) {
            if (char == '/') {
                slashCount += 1
                if (slashCount == 3) break
            }
            i++
        }
        return if (slashCount == 3) {
            uri.substring(startIndex = i)
        } else {
            uri
        }
    }
}
