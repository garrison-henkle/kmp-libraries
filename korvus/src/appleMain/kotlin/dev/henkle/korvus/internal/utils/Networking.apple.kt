package dev.henkle.korvus.internal.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

internal actual val client: HttpClient = HttpClient(engineFactory = Darwin) { configureClient() }

