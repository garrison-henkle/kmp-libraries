package dev.henkle.surreal.internal.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import co.touchlab.kermit.Logger as KLogger

internal expect fun engine(): HttpClientEngineFactory<*>
internal val client: HttpClient by lazy {
    HttpClient(engine()) {
        install(Logging) {
            level = LogLevel.NONE
            logger = object : Logger {
                override fun log(message: String) {
                    KLogger.d("Ktor") { message }
                }
            }
        }
        install(ContentNegotiation) {
            json(json = nullSerializer)
        }
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(format = nullSerializer)
            pingIntervalMillis = 1_800
        }
    }
}

internal val nullSerializer = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = true
}
