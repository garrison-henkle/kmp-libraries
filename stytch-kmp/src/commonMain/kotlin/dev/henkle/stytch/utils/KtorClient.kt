package dev.henkle.stytch.utils

import dev.henkle.stytch.PlatformStytchClient
import dev.henkle.stytch.sessions.SessionRepository
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.toByteArray
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import co.touchlab.kermit.Logger as KLogger
import dev.henkle.stytch.utils.platform as currentPlatform

internal expect fun createKtorClient(
    publicToken: String,
    platform: PlatformStytchClient,
    sessions: SessionRepository,
): HttpClient

@OptIn(ExperimentalEncodingApi::class)
internal fun HttpClientConfig<*>.configurePlugins(
    publicToken: String,
    platform: PlatformStytchClient,
    sessions: SessionRepository,
) {
    install(ContentNegotiation) {
        json(json = jsonClient)
    }

    install(Logging) {
        level = LogLevel.ALL
        logger = object : Logger {
            override fun log(message: String) {
                KLogger.d("StytchKMP Ktor") { message }
            }
        }
    }

    defaultRequest {
        val credentials = "$publicToken:${sessions.opaque ?: publicToken}"
        val base64Credentials = Base64.encode(source = credentials.toByteArray())
        header(
            key = HttpHeaders.Authorization,
            value = "Basic $base64Credentials",
        )

        configurePlatformDefaultRequest(platform = platform, sessions = sessions)
//        if (currentPlatform.isWeb) {
//            host = "web.stytch.com"
//            header (
//                key = HttpHeaders.AccessControlRequestHeaders,
//                value = "authorization,content-type,x-sdk-client,x-sdk-parent-host"
//            )
//
//            header (
//                key = HttpHeaders.AccessControlRequestMethod,
//                value = "GET"
//            )
//
//            header(
//                key = HTTP_HEADER_SDK_PARENT_HOST,
//                value = platform.
//            )
//        }
    }

    // This causes CORS issues on JS and WasmJS
    if (!currentPlatform.isWeb) {
        install(UserAgent) {
            agent = getUserAgent()
        }
    }
}

internal expect fun DefaultRequest.DefaultRequestBuilder.configurePlatformDefaultRequest(
    platform: PlatformStytchClient,
    sessions: SessionRepository,
)

internal const val HTTP_HEADER_SDK_INFO = "X-SDK-Client"
