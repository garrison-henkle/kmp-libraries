package dev.henkle.stytch.model.sdk

/**
 * @property sessionDurationMin if null, a session will not be created
 * @property loginRedirectUrl if null, the default template will be used
 * @property signupRedirectUrl if null, the default template will be used
 * @property resetPasswordRedirectUrl if null, the default template will be used
 */
data class Config(
    val publicToken: String,
    val callbackScheme: String,
    val callbackHost: String,
    val sessionDurationMin: UInt?,
    val oauthTimeoutMin: UInt,
    val loginRedirectUrl: String?,
    val signupRedirectUrl: String?,
    val resetPasswordRedirectUrl: String?,
    val loginExpirationMinutes: UInt,
    val signupExpirationMinutes: UInt,
    val resetPasswordExpirationMinutes: UInt,
    val otpExpirationMinutes: UInt,
) {
    val env: Environment = Environment.fromToken(publicToken = publicToken)

    companion object {
        // Most of the following defaults are taken from the Stytch API documentation
        internal const val LOCALHOST = "http://localhost"
        internal const val DEFAULT_SESSION_DURATION_MIN = 60u
        internal const val DEFAULT_LOGIN_LINK_EXPIRATION_MIN: UInt = 60u
        internal const val DEFAULT_SIGNUP_LINK_EXPIRATION_MIN: UInt = 10_080u // 1 week
        internal const val DEFAULT_RESET_LINK_EXPIRATION_MIN: UInt = 30u
        internal const val DEFAULT_OTP_EXPIRATION_MIN: UInt = 2u
        internal const val DEFAULT_OAUTH_TIMEOUT_MIN: UInt = 10u
        internal const val CODE_VERIFIER_BYTE_COUNT = 32
        internal const val DEFAULT_CALLBACK_SCHEME = "stytchkmp"
        internal const val DEFAULT_CALLBACK_HOST = "callback"
        internal const val DEFAULT_CALLBACK_PATH = "/oauth/callback"
        internal const val DEFAULT_CALLBACK_REDIRECT_PATH = "/oauth/success"
        internal const val DEFAULT_CALLBACK_PORT = 57_981
        internal const val DEFAULT_WEB_PORT = 8080
    }
}
