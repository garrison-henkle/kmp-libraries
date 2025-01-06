package dev.henkle.stytch.model.oauth.response

import dev.henkle.stytch.model.StytchAuthResponseData
import dev.henkle.stytch.model.session.Session
import dev.henkle.stytch.model.user.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OAuthResponse(
    @SerialName(value = "status_code")
    override val status: Int,
    @SerialName(value = "request_id")
    override val requestId: String,
    override val session: Session?,
    @SerialName(value = "session_jwt")
    override val sessionJwt: String,
    @SerialName(value = "session_token")
    override val sessionToken: String,
    override val user: User,
    @SerialName(value = "user_id")
    override val userId: String = user.id,
    @SerialName(value = "oauth_user_registration_id")
    val oauthUserRegistrationId: String,
    @SerialName(value = "provider_subject")
    val providerSubject: String,
    @SerialName(value = "provider_type")
    val providerType: String,
    @SerialName(value = "provider_values")
    val providerValues: ProviderValues,
) : StytchAuthResponseData {
    @Serializable
    data class ProviderValues(
        @SerialName(value = "access_token")
        val accessToken: String,
        @SerialName(value = "refresh_token")
        val refreshToken: String,
        @SerialName(value = "id_token")
        val idToken: String,
        val scopes: List<String>,
    )
}
