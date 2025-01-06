package dev.henkle.stytch.utils

import dev.henkle.stytch.model.sdk.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

private val shutdownScope = CoroutineScope(Dispatchers.IO)
private val shutdownEventFlow = MutableSharedFlow<Unit>()

internal fun launchBrowserAndResponseServer(
    callbackTimeoutMin: UInt,
    callbackEndpointPath: String,
    callbackEndpointPort: Int,
    callbackRedirect: String,
    callback: (uri: String) -> Unit,
    launchBrowser: () -> Unit,
) {
    var serverJob: Job? = null

    shutdownScope.launch {
        shutdownEventFlow.collect {
            oauthServerScope.cancel()
            serverJob?.join()
        }
    }

    serverJob = oauthServerScope.launch {
        createOAuthCallbackServer(
            endpointPath = callbackEndpointPath,
            redirect = callbackRedirect,
            port = callbackEndpointPort,
            onCallbackReceived = { uri ->
                callback(uri)
                shutdownScope.launch {
                    delay(500)
                    shutdownEventFlow.emit(Unit)
                }
            },
        ).start(wait = true)
    }

    launchBrowser()

    shutdownScope.launch {
        delay(timeMillis = callbackTimeoutMin.toLong() * 60 * 1_000)
        shutdownEventFlow.emit(Unit)
    }
}

fun String.isOAuthCallback(
    oauthCallbackPath: String,
    oauthCallbackPort: Int,
): Boolean = startsWith(prefix = "${Config.LOCALHOST}:$oauthCallbackPort$oauthCallbackPath")

internal actual fun getDefaultRedirectUrlInternal(): String =
    "${Config.LOCALHOST}:${Config.DEFAULT_CALLBACK_PORT}${Config.DEFAULT_CALLBACK_PATH}"
