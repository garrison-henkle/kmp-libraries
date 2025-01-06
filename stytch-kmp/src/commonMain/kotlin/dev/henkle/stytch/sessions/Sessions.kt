package dev.henkle.stytch.sessions

import dev.henkle.stytch.model.StytchResult
import dev.henkle.stytch.model.session.SessionAuthenticateParameters
import dev.henkle.stytch.model.session.SessionRevokeParameters
import dev.henkle.stytch.model.session.response.SessionAuthenticateResponse
import dev.henkle.stytch.model.session.response.SessionRevokeResponse

interface Sessions {
    suspend fun authenticate(sessionDurationMin: UInt? = null): StytchResult<SessionAuthenticateResponse>
    suspend fun authenticate(parameters: SessionAuthenticateParameters): StytchResult<SessionAuthenticateResponse>

    suspend fun revoke(forceClear: Boolean = false): StytchResult<SessionRevokeResponse>
    suspend fun revoke(parameters: SessionRevokeParameters): StytchResult<SessionRevokeResponse>
}
