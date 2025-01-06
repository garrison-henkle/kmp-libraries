/*
 * IMPORTANT NOTE: This file must be kept in sync with KtorClient.js until at least Kotlin 2.1.0.
 * The js and wasmJs source sets cannot share code at the moment b/c of compiler restrictions. See:
 * https://youtrack.jetbrains.com/issue/KT-64214
 * https://youtrack.jetbrains.com/issue/KT-62398
 */

package dev.henkle.stytch.utils

import dev.henkle.stytch.PlatformStytchClient
import dev.henkle.stytch.sessions.SessionRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

internal actual fun createKtorClient(
    publicToken: String,
    platform: PlatformStytchClient,
    sessions: SessionRepository,
): HttpClient =
    HttpClient(Js) {
        configurePlugins(
            publicToken = publicToken,
            platform = platform,
            sessions = sessions,
        )
    }
