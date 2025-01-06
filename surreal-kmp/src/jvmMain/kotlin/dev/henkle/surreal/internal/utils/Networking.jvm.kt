package dev.henkle.surreal.internal.utils

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.java.Java

internal actual fun engine(): HttpClientEngineFactory<*> = Java
