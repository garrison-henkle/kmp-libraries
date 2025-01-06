package dev.henkle.korvus.internal.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

internal actual val client: HttpClient = HttpClient(engineFactory = Js) { configureClient() }

