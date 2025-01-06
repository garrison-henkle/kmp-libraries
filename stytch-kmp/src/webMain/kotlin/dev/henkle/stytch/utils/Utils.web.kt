package dev.henkle.stytch.utils

import dev.henkle.stytch.PlatformStytchClient
import dev.henkle.stytch.model.sdk.Config
import dev.henkle.stytch.model.sdk.InfoHeaderData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.encodeToString

internal const val UNDEFINED = "undefined"

internal fun getInfoHeaderJson(platform: PlatformStytchClient): String =
    jsonClient.encodeToString(InfoHeaderData(appIdentifier = platform.domain))

internal actual fun getUserAgent(): String = getBrowserUserAgent()

internal actual fun launchBrowser(
    url: String,
    defaultCallbackScheme: String,
    oauthTimeoutMin: UInt,
    platformStytchClient: PlatformStytchClient,
    callback: (uri: String) -> Unit,
) = setLocation(url = url)

internal actual val IODispatcher: CoroutineDispatcher = Dispatchers.Default

internal actual fun getDefaultRedirectUrlInternal(): String =
    "${Config.LOCALHOST}:${Config.DEFAULT_WEB_PORT}${Config.DEFAULT_CALLBACK_PATH}"

internal expect fun setLocation(url: String)

internal expect fun getScreenSize(): Pair<Int, Int>

internal expect fun getBrowserUserAgent(): String
