package dev.henkle.stytch.sessions

import dev.henkle.stytch.model.StytchAuthResponseData
import dev.henkle.stytch.model.StytchResult
import dev.henkle.stytch.model.sdk.Config
import dev.henkle.stytch.model.session.SessionAuthenticateParameters
import dev.henkle.stytch.model.session.SessionRevokeParameters
import dev.henkle.stytch.model.session.request.SessionAuthenticateRequest
import dev.henkle.stytch.model.session.response.SessionAuthenticateResponse
import dev.henkle.stytch.model.session.response.SessionRevokeResponse
import dev.henkle.stytch.utils.StytchHTTPClient
import dev.henkle.stytch.utils.ext.ifSuccessfulAuth

internal class SessionsImpl(
    private val client: StytchHTTPClient,
    private val config: Config,
    private val onAuthenticate: (StytchAuthResponseData) -> Unit,
    private val onRevoke: () -> Unit,
) : Sessions {
    override suspend fun authenticate(
        sessionDurationMin: UInt?,
    ): StytchResult<SessionAuthenticateResponse> =
        authenticate(parameters = SessionAuthenticateParameters(sessionDurationMin = sessionDurationMin))

    override suspend fun authenticate(
        parameters: SessionAuthenticateParameters,
    ): StytchResult<SessionAuthenticateResponse> =
        client.post<SessionAuthenticateRequest, SessionAuthenticateResponse>(
            path = PATH_SESSIONS_AUTHENTICATE,
            body = SessionAuthenticateRequest(
                sessionDurationMinutes = parameters.sessionDurationMin ?: config.sessionDurationMin,
            ),
        ).ifSuccessfulAuth(perform = onAuthenticate)

    override suspend fun revoke(forceClear: Boolean): StytchResult<SessionRevokeResponse> =
        revoke(parameters = SessionRevokeParameters(forceClear = forceClear))

    override suspend fun revoke(
        parameters: SessionRevokeParameters,
    ): StytchResult<SessionRevokeResponse> =
        client.post<Unit, SessionRevokeResponse>(path = PATH_SESSIONS_REVOKE)
            .also { result ->
                if (result.isSuccess || parameters.forceClear) {
                    onRevoke()
                }
            }

    companion object {
        private const val PATH_SESSIONS_REVOKE = "sessions/revoke"
        private const val PATH_SESSIONS_AUTHENTICATE = "sessions/authenticate"
    }
}
