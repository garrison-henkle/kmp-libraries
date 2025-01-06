package dev.henkle.stytch.utils

import co.touchlab.kermit.Logger
import dev.henkle.stytch.model.sdk.Config

internal fun String.isOAuthCallback(
    config: Config,
    overrideOAuthCallbackScheme: String?,
    overrideOAuthCallbackHost: String?,
): Boolean {
    val scheme = overrideOAuthCallbackScheme ?: config.callbackScheme
    val host = overrideOAuthCallbackHost ?: config.callbackHost
    Logger.e("garrison") { "checking if it starts with $scheme://$host" }
    return startsWith(prefix = "$scheme://$host")
}

internal actual fun getDefaultRedirectUrlInternal(): String =
    "${Config.DEFAULT_CALLBACK_SCHEME}://${Config.DEFAULT_CALLBACK_HOST}"
