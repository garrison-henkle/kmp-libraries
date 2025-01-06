package dev.henkle.stytch.model.user

import dev.henkle.stytch.model.session.Session
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class User(
    @SerialName(value = "user_id")
    val id: String,
    val name: Name?,
    @SerialName(value = "trusted_metadata")
    val trustedMetadata: JsonObject,
    @SerialName(value = "untrusted_metadata")
    val untrustedMetadata: JsonObject,
    val emails: List<Email>,
    @SerialName(value = "phone_numbers")
    val phoneNumbers: List<PhoneNumber>,
    val password: Password?,
    @SerialName(value = "providers")
    val oauthProviders: List<OAuthProvider>,
    @SerialName(value = "webauthn_registrations")
    val webauthnRegistrations: List<WebAuthnRegistration>,
    @SerialName(value = "biometric_registrations")
    val biometricRegistrations: List<BiometricRegistration>,
    val totps: List<TOTP>,
    @SerialName(value = "crypto_wallets")
    val cryptoWallets: List<Session.Factor.CryptoWallet>,
    val status: Status,
    @SerialName(value = "created_at")
    val createdAt: Instant,
) {
    @Serializable
    data class Name(
        @SerialName(value = "first_name")
        val firstName: String? = null,
        @SerialName(value = "middle_name")
        val middleName: String? = null,
        @SerialName(value = "last_name")
        val lastName: String? = null,
    )

    @Serializable
    data class Email(
        @SerialName(value = "email_id")
        val id: String,
        val email: String,
        val verified: Boolean,
    )

    @Serializable
    data class PhoneNumber(
        @SerialName(value = "phone_id")
        val id: String,
        @SerialName(value = "phone_number")
        val phoneNumber: String,
        val verified: Boolean,
    )

    @Serializable
    data class Password(
        @SerialName(value = "password_id")
        val id: String,
        @SerialName(value = "requires_reset")
        val requiresReset: Boolean,
    )

    @Serializable
    data class WebAuthnRegistration(
        @SerialName(value = "webauthn_registration_id")
        val id: String
    )

    @Serializable
    data class OAuthProvider(
        @SerialName(value = "oauth_user_registration_id")
        val userRegistrationId: String,
        @SerialName(value = "provider_subject")
        val subject: String,
        @SerialName(value = "provider_type")
        val type: String,
        @SerialName(value = "profile_picture_url")
        val profilePictureUrl: String? = null,
        val locale: String? = null,
    )

    @Serializable
    data class BiometricRegistration(
        @SerialName(value = "biometric_registration_id")
        val id: String,
        val verified: Boolean,
    )

    @Serializable
    data class TOTP(
        @SerialName(value = "totp_id")
        val id: String,
        val verified: Boolean,
    )

    @Serializable
    enum class Status {
        @SerialName(value = "active")
        Active,
        @SerialName(value = "pending")
        Pending,
    }
}
