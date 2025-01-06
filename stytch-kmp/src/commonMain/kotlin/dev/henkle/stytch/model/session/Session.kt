package dev.henkle.stytch.model.session

import kotlinx.datetime.Instant
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonObject

@Serializable
data class Session(
    @SerialName(value = "session_id")
    val id: String,
    @SerialName(value = "user_id")
    val userId: String,
    @SerialName(value = "authentication_factors")
    val authenticationFactors: List<AuthenticationFactor>,
    @SerialName(value = "started_at")
    val startedAt: Instant,
    @SerialName(value = "last_accessed_at")
    val lastAccessedAt: Instant,
    @SerialName(value = "expires_at")
    val expiresAt: Instant,
    val attributes: JsonObject = JsonObject(content = emptyMap()),
    @SerialName(value = "custom_claims")
    val customClaims: JsonObject = JsonObject(content = emptyMap()),
) {
    @Serializable
    @OptIn(ExperimentalSerializationApi::class)
    data class AuthenticationFactor(
        @SerialName(value = "delivery_method")
        val deliveryMethod: String,
        @SerialName(value = "last_authenticated_at")
        val lastAuthenticatedAt: Instant,
        @SerialName(value = "created_at")
        val createdAt: Instant,
        @SerialName(value = "updated_at")
        val updatedAt: Instant,
        @SerialName(value = "type")
        val type: String,
        @JsonNames(
            "email_factor",
            "phone_number_factor",
            "google_oauth_factor",
            "microsoft_oauth_factor",
            "apple_oauth_factor",
            "github_oauth_factor",
            "gitlab_oauth_factor",
            "facebook_oauth_factor",
            "discord_oauth_factor",
            "salesforce_oauth_factor",
            "yahoo_oauth_factor",
            "slack_oauth_factor",
            "amazon_oauth_factor",
            "bitbucket_oauth_factor",
            "linkedin_oauth_factor",
            "coinbase_oauth_factor",
            "twitch_oauth_factor",
            "twitter_oauth_factor",
            "tiktok_oauth_factor",
            "snapchat_oauth_factor",
            "figma_oauth_factor",
            "webauthn_factor",
            "biometric_factor",
            "authenticator_app_factor",
            "recovery_code_factor",
            "crypto_wallet_factor"
        )
        val factor: Factor,
    )

    @Serializable(with = Factor.Serializer::class)
    sealed interface Factor {
        @Serializable
        data class Email(
            @SerialName(value = "email_id")
            val emailId: String,
            @SerialName(value = KEY_EMAIL_ADDRESS)
            val emailAddress: String,
        ) : Factor

        @Serializable
        data class Phone(
            @SerialName(value = "phone_id")
            val phoneId: String,
            @SerialName(value = KEY_PHONE_NUMBER)
            val phoneNumber: String,
        ) : Factor

        @Serializable
        data class OAuth(
            val id: String,
            @SerialName(value = "email_id")
            val emailId: String? = null,
            @SerialName(value = KEY_OAUTH_PROVIDER_SUBJECT)
            val providerSubject: String,
        ) : Factor

        @Serializable
        data class WebAuthn(
            @SerialName(value = KEY_WEBAUTHN_ID)
            val webauthnRegistrationId: String,
            val domain: String,
            @SerialName(value = "user_agent")
            val userAgent: String,
        ) : Factor

        @Serializable
        data class Biometric(
            @SerialName(value = KEY_BIOMETRIC_ID)
            val biometricRegistrationId: String,
        ) : Factor

        @Serializable
        data class AuthenticatorApp(
            @SerialName(value = KEY_TOTP_ID)
            val totpId: String,
        ) : Factor

        @Serializable
        data class RecoveryCode(
            @SerialName(value = KEY_TOTP_RECOVERY_ID)
            val totpRecoveryCodeId: String,
        ) : Factor


        @Serializable
        data class CryptoWallet(
            @SerialName(value = KEY_CRYPTO_WALLET_ID)
            val cryptoWalletId: String,
            @SerialName(value = "crypto_wallet_address")
            val cryptoWalletAddress: String,
            @SerialName(value = "crypto_wallet_type")
            val cryptoWalletType: String,
        ) : Factor

        // TODO: this is a bad approach, but the alternative is overly complicated (custom
        //  Session.AuthenticationFactor KSerializer + class descriptors for both
        //  Session.AuthenticationFactor and Session.Factor)
        //  Issue with same problem: https://github.com/Kotlin/kotlinx.serialization/issues/1384
        class Serializer : JsonContentPolymorphicSerializer<Factor>(Factor::class) {
            override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Factor> {
                if (element !is JsonObject) {
                    throw IllegalArgumentException(
                        "Session.Factor deserializer expected a json object!"
                    )
                }
                return when {
                    element.containsKey(KEY_EMAIL_ADDRESS) -> Email.serializer()
                    element.containsKey(KEY_PHONE_NUMBER) -> Phone.serializer()
                    element.containsKey(KEY_OAUTH_PROVIDER_SUBJECT) -> OAuth.serializer()
                    element.containsKey(KEY_WEBAUTHN_ID) -> WebAuthn.serializer()
                    element.containsKey(KEY_BIOMETRIC_ID) -> Biometric.serializer()
                    element.containsKey(KEY_TOTP_ID) -> AuthenticatorApp.serializer()
                    element.containsKey(KEY_TOTP_RECOVERY_ID) -> RecoveryCode.serializer()
                    element.containsKey(KEY_CRYPTO_WALLET_ID) -> CryptoWallet.serializer()
                    else -> throw IllegalArgumentException(
                        "Unrecognized Factor type in Session.AuthenticationFactor",
                    )
                }
            }
        }

        companion object {
            private const val KEY_EMAIL_ADDRESS = "email_address"
            private const val KEY_PHONE_NUMBER = "phone_number"
            private const val KEY_OAUTH_PROVIDER_SUBJECT = "provider_subject"
            private const val KEY_WEBAUTHN_ID = "webauthn_registration_id"
            private const val KEY_BIOMETRIC_ID = "biometric_registration_id"
            private const val KEY_TOTP_ID = "totp_id"
            private const val KEY_TOTP_RECOVERY_ID = "totp_recovery_code_id"
            private const val KEY_CRYPTO_WALLET_ID = "crypto_wallet_id"
        }
    }
}