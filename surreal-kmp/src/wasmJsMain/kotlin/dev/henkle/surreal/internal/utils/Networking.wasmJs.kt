package dev.henkle.surreal.internal.utils

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js

internal actual fun engine(): HttpClientEngineFactory<*> = Js
