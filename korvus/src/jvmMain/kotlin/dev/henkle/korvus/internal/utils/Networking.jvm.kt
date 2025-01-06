package dev.henkle.korvus.internal.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java

internal actual val client: HttpClient = HttpClient(engineFactory = Java) { configureClient() }
