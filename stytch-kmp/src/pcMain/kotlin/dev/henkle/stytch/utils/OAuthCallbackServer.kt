package dev.henkle.stytch.utils

import dev.henkle.stytch.model.sdk.Config
import io.ktor.http.HttpStatusCode
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope

private const val QUERY_PARAM_TOKEN = "token"
private const val RESPONSE_OK = "Authentication successful! Please close this tab and return to the application."
private const val RESPONSE_BAD_REQUEST = "Authentication failed! Please return to the application and try again."

fun CoroutineScope.createOAuthCallbackServer(
    endpointPath: String,
    redirect: String,
    port: Int,
    onCallbackReceived: suspend (uri: String) -> Unit,
): EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration> = embeddedServer(
        factory = CIO,
        host = "127.0.0.1",
        port = port,
    ) {
        val externalRedirect = redirect.startsWith(prefix = "http")
        routing {
            if (!externalRedirect) {
                get(path = redirect) {
                    call.respond(status = HttpStatusCode.OK, message = RESPONSE_OK)
                }
            }

            get(path = endpointPath) {
                call.request.queryParameters[QUERY_PARAM_TOKEN]?.also {
                    val baseUrl = "${Config.LOCALHOST}:$port"
                    val redirectUrl = if(externalRedirect) redirect else "$baseUrl$redirect"
                    onCallbackReceived("$baseUrl${call.request.uri}")
                    call.respondRedirect(url = redirectUrl)
                } ?: call.respond(status = HttpStatusCode.BadRequest, message = RESPONSE_BAD_REQUEST)
            }
        }
    }
