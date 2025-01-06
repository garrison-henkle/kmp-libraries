package dev.henkle.stytch

import dev.henkle.stytch.StytchClient.UriParsingResult
import dev.henkle.stytch.StytchClient.UriType
import dev.henkle.stytch.model.sdk.Config
import dev.henkle.stytch.utils.isOAuthCallback
import kotlinx.coroutines.flow.MutableStateFlow

actual class PlatformStytchClient internal constructor() {
    internal var appPackageName: String? = null
    internal var appVersionString: String? = null
    internal var deviceModel: String? = null
    internal var oauthCallbackEndpoint: String = Config.DEFAULT_CALLBACK_PATH
    internal var oauthCallbackPort: Int = Config.DEFAULT_CALLBACK_PORT
    internal var oauthCallbackRedirect: String = Config.DEFAULT_CALLBACK_REDIRECT_PATH

    internal actual fun isHandledUri(uri: String): UriType? =
        when {
            uri.isOAuthCallback(
                oauthCallbackPath = oauthCallbackEndpoint,
                oauthCallbackPort = oauthCallbackPort,
            ) -> UriType.OAuth

            else -> null
        }


    internal actual suspend fun handleUri(uri: String, type: UriType): UriParsingResult? =
        when(type) {
            UriType.OAuth -> parseOAuthToken(uri = uri)
        }

    fun configure(
        appPackageName: String,
        appVersionString: String,
        deviceModel: String,
        oauthCallbackEndpoint: String = Config.DEFAULT_CALLBACK_PATH,
        oauthCallbackPort: Int = Config.DEFAULT_CALLBACK_PORT,
        oauthCallbackRedirect: String = Config.DEFAULT_CALLBACK_REDIRECT_PATH,
    ) {
        this.appPackageName = appPackageName
        this.appVersionString = appVersionString
        this.deviceModel = deviceModel
        this.oauthCallbackEndpoint = oauthCallbackEndpoint
        this.oauthCallbackPort = oauthCallbackPort
        this.oauthCallbackRedirect = oauthCallbackRedirect
    }

    val currentScreenSizePx = MutableStateFlow(0 to 0)
}
