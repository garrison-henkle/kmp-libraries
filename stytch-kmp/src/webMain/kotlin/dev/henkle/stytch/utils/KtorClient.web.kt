package dev.henkle.stytch.utils

import dev.henkle.stytch.PlatformStytchClient
import dev.henkle.stytch.sessions.SessionRepository
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val HOST = "web.stytch.com"
private const val HTTP_HEADER_SDK_PARENT_HOST = "X-SDK-Parent-Host"
private const val HTTP_HEADER_TE_VALUE = "trailers"
private const val HTTP_HEADER_ACCESS_CONTROL_REQUEST_VALUE =
    "authorization,content-type,x-sdk-client,x-sdk-parent-host"

@OptIn(ExperimentalEncodingApi::class)
internal actual fun DefaultRequest.DefaultRequestBuilder.configurePlatformDefaultRequest(
    platform: PlatformStytchClient,
    sessions: SessionRepository,
) {
    host = HOST

    val infoHeaderData = getInfoHeaderJson(platform = platform)
    header(
        key = HTTP_HEADER_SDK_INFO,
        value = Base64.encode(source = infoHeaderData.encodeToByteArray()),
    )
    header (
        key = HttpHeaders.AccessControlRequestHeaders,
        value = HTTP_HEADER_ACCESS_CONTROL_REQUEST_VALUE,
    )
    header(
        key = HTTP_HEADER_SDK_PARENT_HOST,
        value = platform.domain,
    )
    header(
        key = HttpHeaders.TE,
        value = HTTP_HEADER_TE_VALUE,
    )
}
