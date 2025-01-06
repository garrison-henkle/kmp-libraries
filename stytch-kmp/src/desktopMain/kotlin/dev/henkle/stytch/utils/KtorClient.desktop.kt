package dev.henkle.stytch.utils

import dev.henkle.stytch.PlatformStytchClient
import dev.henkle.stytch.sessions.SessionRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java

internal actual fun createKtorClient(
    publicToken: String,
    platform: PlatformStytchClient,
    sessions: SessionRepository,
): HttpClient =
    HttpClient(Java) {
        configurePlugins(
            publicToken = publicToken,
            platform = platform,
            sessions = sessions,
        )
    }