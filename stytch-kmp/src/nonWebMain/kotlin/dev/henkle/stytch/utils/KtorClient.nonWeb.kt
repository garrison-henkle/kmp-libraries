package dev.henkle.stytch.utils

import dev.henkle.stytch.PlatformStytchClient
import dev.henkle.stytch.sessions.SessionRepository
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.request.header
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
internal actual fun DefaultRequest.DefaultRequestBuilder.configurePlatformDefaultRequest(
    platform: PlatformStytchClient,
    sessions: SessionRepository,
) {
    header(
        key = HTTP_HEADER_SDK_INFO,
        value = Base64.encode(
            source = getInfoHeaderData(platform = platform).json.encodeToByteArray(),
        ),
    )
}
