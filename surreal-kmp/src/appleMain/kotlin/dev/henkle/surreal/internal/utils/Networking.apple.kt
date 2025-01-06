package dev.henkle.surreal.internal.utils

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

internal actual fun engine(): HttpClientEngineFactory<*> = Darwin

