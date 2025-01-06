package dev.henkle.stytch.utils

import co.touchlab.kermit.Logger
import dev.henkle.stytch.model.StytchError
import dev.henkle.stytch.model.StytchResponseData
import dev.henkle.stytch.model.StytchResponseWrapper
import dev.henkle.stytch.model.StytchResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlinx.coroutines.withContext

internal class StytchHTTPClient(
    val httpClient: HttpClient,
    val onUnauthorized: () -> Unit,
) {
    suspend inline fun <reified B: Any, reified R: StytchResponseData> post(
        path: String,
        body: B? = null,
    ): StytchResult<R> = withContext(IODispatcher) {
        try {
            val response = httpClient.post {
                url {
                    protocol = URLProtocol.HTTPS
                    host = STYTCH_API_DOMAIN
                    appendPathSegments(STYTCH_API_BASE_PATH, path)
                    Logger.e("garrison") { "StytchHTTPClient hitting ${it.buildString()}" }
                }
                contentType(type = ContentType.Application.Json)
                if (body != null) {
                    setBody(body)
                }
                header (
                    key = HttpHeaders.AccessControlRequestMethod,
                    value = HttpMethod.Post.value,
                )
            }
            if (response.status == HttpStatusCode.OK) {
                StytchResult.Success(result = response.body<StytchResponseWrapper<R>>().data)
            } else {
                if (response.status == HttpStatusCode.Unauthorized) {
                    onUnauthorized()
                }
                StytchResult.Failure(error = response.body<StytchError.APIError>())
            }
        } catch (ex: Exception) {
            Logger.e("garrison") { "StytchHTTPClient ex: $ex\n${ex.stackTraceToString()}" }
            StytchResult.Failure(
                error = StytchError.Error(
                    message = ex.message ?: ex.toString(),
                    ex = ex,
                ),
            )
        }
    }

    companion object {
        private const val STYTCH_API_DOMAIN = "web.stytch.com"
        private const val STYTCH_API_BASE_PATH = "/sdk/v1"
    }
}
