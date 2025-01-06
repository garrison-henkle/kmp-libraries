package dev.henkle.stytch

import dev.henkle.stytch.StytchClient.UriParsingResult
import dev.henkle.stytch.StytchClient.UriType

private const val QUERY_PARAM_TOKEN = "token"

expect class PlatformStytchClient {
    internal fun isHandledUri(uri: String): UriType?
    internal suspend fun handleUri(uri: String, type: UriType): UriParsingResult?
}

// TODO: move to Utils.kt
internal fun parseOAuthToken(uri: String): UriParsingResult? =
    uri.substringAfter(delimiter = '?')
        .split('&')
        .firstOrNull { it.startsWith(prefix = QUERY_PARAM_TOKEN) }
        ?.split('=')
        ?.getOrNull(index = 1)
        ?.let { UriType.OAuth(token = it) }
