package dev.henkle.stytch.utils.ext

import dev.henkle.stytch.model.StytchAuthResponseData
import dev.henkle.stytch.model.StytchError
import dev.henkle.stytch.model.StytchResult

internal fun <T: StytchAuthResponseData> StytchResult<T>.ifSuccessfulAuth(
    perform: (StytchAuthResponseData) -> Unit,
): StytchResult<T> = apply {
    if (this is StytchResult.Success<T>) {
        perform(result)
    }
}
