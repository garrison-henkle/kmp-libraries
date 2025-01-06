@file:Suppress("ObjectPropertyName")

package dev.henkle.stytch.utils

import dev.henkle.stytch.PlatformStytchClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

internal expect val IODispatcher: CoroutineDispatcher

private var _oauthServerScope: CoroutineScope? = null
internal val oauthServerScope: CoroutineScope
    get() = _oauthServerScope ?: CoroutineScope(IODispatcher).also { _oauthServerScope = it }
private var defaultRedirectUrl: String? = null
internal const val UNKNOWN = "unknown"

internal expect val platform: Platform

internal expect fun getUserAgent(): String

internal expect fun launchBrowser(
    url: String,
    defaultCallbackScheme: String,
    oauthTimeoutMin: UInt,
    platformStytchClient: PlatformStytchClient,
    callback: (uri: String) -> Unit,
)

internal expect fun getDefaultRedirectUrlInternal(): String

internal fun getDefaultRedirectUrl(): String = defaultRedirectUrl
    ?: getDefaultRedirectUrlInternal().also { defaultRedirectUrl = it }

@OptIn(ExperimentalSerializationApi::class)
internal val jsonClient = Json {
    explicitNulls = false
    ignoreUnknownKeys = true
    encodeDefaults = true
}