package dev.henkle.korvus.internal.utils

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal expect val client: HttpClient
internal fun <T : HttpClientEngineConfig> HttpClientConfig<T>.configureClient() {
    install(Logging)
    install(ContentNegotiation) {
        json(json = nullSerializer)
    }
}

internal val nullSerializer = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = true
}
