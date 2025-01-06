package dev.henkle.stytch.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface StytchError {
    /**
     * An error response returned by the Stytch API.
     *
     * There is unfortunately no (practical) way to create sealed interfaces for each endpoint's
     * error responses because the possible error responses are not documented. If you'd also like
     * the option to know all the possible error responses ahead of time, let Stytch know ¯\_(ツ)_/¯
     *
     * @see <a href="">Stytch error documentation (sorted by status code in sidebar)</a>
     */
    @Serializable
    data class APIError(
        @SerialName(value = "status_code")
        val status: Int,
        @SerialName(value = "request_id")
        val requestId: String = "",
        @SerialName(value = "error_type")
        val type: String,
        @SerialName(value = "error_message")
        val message: String,
        @SerialName(value = "error_url")
        val url: String,
    ) : StytchError

    /**
     * An error whose source is not the Stytch API i.e. internal SDK errors
     */
    data class Error(val message: String, val ex: Exception? = null) : StytchError
}