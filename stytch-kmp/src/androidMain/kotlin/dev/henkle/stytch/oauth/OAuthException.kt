package dev.henkle.stytch.oauth

sealed class OAuthException(private val msg: String) : Exception(msg) {
    /**
     * Indicates that no suitable browser was found on this device, and OAuth authentication cannot proceed.
     */
    data object NoBrowserFound : OAuthException(msg = "No supported browser was found on this device") {
        private fun readResolve(): Any = NoBrowserFound
    }

    /**
     * Indicates that no URI was found in the activity state, and OAuth authentication cannot proceed.
     */
    data object NoUriFound : OAuthException(msg = "No OAuth URI could be found in the bundle") {
        private fun readResolve(): Any = NoUriFound
    }

    /**
     * Indicates that the user canceled the OAuth flow. This is safe to ignore.
     */
    data object UserCanceled : OAuthException(msg = "The user canceled the OAuth flow") {
        private fun readResolve(): Any = UserCanceled
    }

    companion object {
        /**
         * A string identifying the class of this Exception for serializing/deserializing the error within an Intent
         */
        const val KEY_OAUTH_EXCEPTION = "dev.henkle.stytch.oauth.SSOException"
    }
}