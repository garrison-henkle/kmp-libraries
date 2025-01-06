package dev.henkle.korvus.internal.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

internal actual val client: HttpClient = HttpClient(engineFactory = Android) { configureClient() }

