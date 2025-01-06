package dev.henkle.surreal.internal.utils

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

// have to use OkHttp b/c the Android engine doesn't support web sockets
internal actual fun engine(): HttpClientEngineFactory<*> = OkHttp
